package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable

/**
 * [LottieComposition] from network [url]
 *
 * @param format animation format (JSON/dotLottie). Composition spec will try to guess format if format is not specified.
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 *
 * URL assets will be automatically prepared with [NetworkAssetsManager]
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun LottieCompositionSpec.Companion.Url(
    url : String,
    format: LottieAnimationFormat = LottieAnimationFormat.Undefined,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieCompositionSpec = Url(
    url = url,
    request = DefaultHttpRequest,
    format = format,
    cacheStrategy = cacheStrategy,
)


