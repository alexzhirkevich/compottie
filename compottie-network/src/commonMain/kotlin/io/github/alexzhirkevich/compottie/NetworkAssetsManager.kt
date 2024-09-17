package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager

/**
 * Asset manager that load images from web.
 *
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkAssetsManager(
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManager(
    request = DefaultHttpRequest,
    cacheStrategy = cacheStrategy
)
