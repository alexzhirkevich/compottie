package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.util.toByteArray
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [LottieComposition] from network [url]
 *
 * @param format animation format (JSON/dotLottie). Composition spec will try to guess format if format is not specified.
 * @param client http client used for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@Stable
fun LottieCompositionSpec.Companion.Url(
    url : String,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieCompositionSpec = NetworkCompositionSpec(
    url = url,
    format = format,
    client = client,
    cacheStrategy = cacheStrategy,
    request = request
)

@Stable
private class NetworkCompositionSpec(
    private val url : String,
    private val format: LottieAnimationFormat,
    private val client : HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
    private val request : NetworkRequest,
) : LottieCompositionSpec {

    override val key: String
        get() = "url_${url.hashCode()}"

    private val assetsManager = NetworkAssetsManager(client, request, cacheStrategy)
    private val fontManager = NetworkFontManager(client, request, cacheStrategy)

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(cacheKey : Any?): LottieComposition {
        return withContext(ioDispatcher()) {
            mainMutex.withLock { mutexByUrl.getOrPut(url) { Mutex() } }.withLock {
                try {
                    LottieComposition.getOrCreate(cacheKey) {
                        try {
                            Compottie.logger?.info("Searching for animation in cache...")
                            cacheStrategy.load(url)?.let {
                                Compottie.logger?.info("Animation was found in cache. Parsing...")
                                return@getOrCreate it.decodeToLottieComposition(format).also {
                                    Compottie.logger?.info("Animation was successfully loaded from cache")
                                }
                            } ?: run {
                                Compottie.logger?.info("Animation wasn't found in cache")
                            }
                        } catch (t : UnsupportedFileSystemException) {
                            Compottie.logger?.info("File system cache is disabled for this strategy on the current platform")
                        } catch (_: Throwable) {
                            Compottie.logger?.info("Failed to load or decode animation from cache")
                        }

                        Compottie.logger?.info("Fetching animation from web...")

                        val bytes = try {
                            val response = request(client, Url(url)).execute()

                            if (!response.status.isSuccess()) {
                                Compottie.logger?.info("Animation request failed with ${response.status.value} status code")
                                throw ClientRequestException(response, response.bodyAsText())
                            }

                            response.bodyAsChannel().toByteArray()
                        } catch (t : ClientRequestException){
                            Compottie.logger?.info("Animation request failed with ${t.response.status.value} status code")
                            throw t
                        }
                        Compottie.logger?.info("Animation was loaded from web. Parsing...")

                        val composition = bytes.decodeToLottieComposition(format)
                        Compottie.logger?.info("Animation was successfully loaded from web. Caching...")

                        try {
                            cacheStrategy.save(url, bytes)
                            Compottie.logger?.info("Animation was successfully saved to cache")
                        } catch (t : UnsupportedFileSystemException) {
                          Compottie.logger?.info("File system cache is disabled for this strategy on the current platform")
                        } catch (t: Throwable) {
                            Compottie.logger?.error(
                                "Failed to cache animation",
                                t
                            )
                        }
                        composition
                    }
                } finally {
                    mainMutex.withLock {
                        mutexByUrl.remove(url)
                    }
                }
            }.apply {
                launch {
                    prepareAssets(assetsManager)
                }
                launch {
                    prepareFonts(fontManager)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkCompositionSpec

        if (url != other.url) return false
        if (format != other.format) return false
        if (client != other.client) return false
        if (cacheStrategy != other.cacheStrategy) return false
        if (request != other.request) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + client.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        result = 31 * result + request.hashCode()
        return result
    }

    companion object {
        private val mainMutex = Mutex()
        private val mutexByUrl = mutableMapOf<String, Mutex>()
    }
}

