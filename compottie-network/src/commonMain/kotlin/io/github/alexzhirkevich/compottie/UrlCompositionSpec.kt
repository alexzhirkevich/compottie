package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.network.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.network.LottieCacheStrategy
import io.github.alexzhirkevich.compottie.network.Url as CoreUrl

/**
 * [LottieComposition] from network [url]
 *
 * @param request network request used for loading animation
 * @param format animation format (JSON/dotLottie). Composition spec will try to guess format if format is not specified.
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun LottieCompositionSpec.Companion.Url(
    url : String,
    request: suspend (url: String) -> ByteArray = DefaultHttpRequest,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieCompositionSpec = CoreUrl(
    url = url,
    format = format,
    request = request,
    cacheStrategy = cacheStrategy,
)


