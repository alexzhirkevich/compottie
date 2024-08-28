package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [LottieComposition] from network [url]
 *
 * @param request network request used for loading animations
 * @param format animation format (JSON/dotLottie). Composition spec will try to guess format if format is not specified.
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun LottieCompositionSpec.Companion.Url(
    url : String,
    request: suspend (url: String) -> ByteArray,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieCompositionSpec = NetworkCompositionSpec(
    url = url,
    format = format,
    request = request,
    cacheStrategy = cacheStrategy,
)

@Stable
private class NetworkCompositionSpec(
    private val url : String,
    private val format: LottieAnimationFormat,
    private val request : suspend (url: String) -> ByteArray,
    private val cacheStrategy: LottieCacheStrategy,
) : LottieCompositionSpec {

    override val key: String
        get() = "url_$url"

    private val assetsManager = NetworkAssetsManager(
        request = request,
        cacheStrategy = cacheStrategy
    )

    private val fontManager = NetworkFontManager(
        request = request,
        cacheStrategy = cacheStrategy
    )

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(): LottieComposition {
        return withContext(ioDispatcher()) {

            val (_, bytes) = networkLoad(request, cacheStrategy, url)

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
        if (request != other.request) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}

