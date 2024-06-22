import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.DefaultHttpClient
import io.github.alexzhirkevich.compottie.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.GetRequest
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.L
import io.github.alexzhirkevich.compottie.LottieAnimationFormat
import io.github.alexzhirkevich.compottie.LottieCacheStrategy
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.NetworkAssetsManager
import io.github.alexzhirkevich.compottie.NetworkRequest
import io.github.alexzhirkevich.compottie.ioDispatcher
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.util.toByteArray
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [LottieComposition] from network [url]
 *
 * @param format animation format. Composition spec will try to guess format if format is not specified.
 * @param client http client user for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system tmp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@Stable
fun LottieCompositionSpec.Companion.Url(
    url : String,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
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
    private val cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
    private val request : NetworkRequest,
) : LottieCompositionSpec {

    private val assetsManager = NetworkAssetsManager(client, cacheStrategy, request)

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(cacheKey : Any?): LottieComposition {
        return withContext(ioDispatcher()) {
            mainMutex.withLock { mutexByUrl.getOrPut(url) { Mutex() } }.withLock {
                try {
                    LottieComposition.getOrCreate(cacheKey) {
                        try {
                            cacheStrategy.load(url)?.let {
                                return@getOrCreate it.decodeLottieComposition(format)
                            }
                        } catch (_: Throwable) {
                        }

                        val response = request(client, Url(url)).execute()

                        if (!response.status.isSuccess()) {
                            throw ClientRequestException(response, response.bodyAsText())
                        }

                        val bytes = response.bodyAsChannel().toByteArray()

                        val composition = bytes.decodeLottieComposition(format)

                        try {
                            cacheStrategy.save(url, bytes)
                        } catch (t: Throwable) {
                            L.logger.error(
                                "Url composition spec failed to cache downloaded animation",
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
                prepareAssets(assetsManager)
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


private suspend fun ByteArray.decodeLottieComposition(
    format: LottieAnimationFormat
) : LottieComposition {
    return when (format) {
        LottieAnimationFormat.Json -> LottieCompositionSpec.JsonString(decodeToString()).load()
        LottieAnimationFormat.DotLottie -> LottieCompositionSpec.DotLottie(this).load()
        LottieAnimationFormat.Undefined -> {
            try {
                decodeLottieComposition(LottieAnimationFormat.Json)
            } catch (t: Throwable) {
                decodeLottieComposition(LottieAnimationFormat.DotLottie)
            }
        }
    }
}