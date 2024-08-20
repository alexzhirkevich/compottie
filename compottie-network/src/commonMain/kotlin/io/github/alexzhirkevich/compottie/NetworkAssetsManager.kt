package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.network.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.network.LottieCacheStrategy

/**
 * Asset manager that load images from web using http [client].
 *
 * @param request network request used for loading assets
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkAssetsManager(
    request : suspend (url: String) -> ByteArray = DefaultHttpRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = io.github.alexzhirkevich.compottie.network.NetworkAssetsManager(request, cacheStrategy)
