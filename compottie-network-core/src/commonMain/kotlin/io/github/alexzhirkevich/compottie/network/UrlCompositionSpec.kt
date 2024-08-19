package io.github.alexzhirkevich.compottie.network

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.LottieAnimationFormat
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.decodeToLottieComposition
import io.github.alexzhirkevich.compottie.ioDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * [LottieComposition] from network [url]
 *
 * @param format animation format (JSON/dotLottie). Composition spec will try to guess format if format is not specified.
 * @param client http client used for loading animation
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun LottieCompositionSpec.Companion.Url(
    url : String,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieCompositionSpec = NetworkCompositionSpec(
    url = url,
    format = format,
    client = client,
    cacheStrategy = cacheStrategy,
)

@Stable
private class NetworkCompositionSpec(
    private val url : String,
    private val format: LottieAnimationFormat,
    private val client : HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
) : LottieCompositionSpec {

    override val key: String
        get() = "url_$url"

    private val assetsManager = NetworkAssetsManager(client, cacheStrategy)
    private val fontManager = NetworkFontManager(client, cacheStrategy)

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(): LottieComposition {
        return withContext(ioDispatcher()) {

            val (_, bytes) = networkLoad(client, cacheStrategy, url)

            checkNotNull(bytes?.decodeToLottieComposition(format)){
                "Failed to load animation $url"
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

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + client.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }

    companion object {
        private val mainMutex = Mutex()
        private val mutexByUrl = mutableMapOf<String, Mutex>()
    }
}

