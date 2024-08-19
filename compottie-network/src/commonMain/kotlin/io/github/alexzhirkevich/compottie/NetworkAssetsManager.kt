package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.network.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.network.HttpClient
import io.github.alexzhirkevich.compottie.network.LottieCacheStrategy

/**
 * Asset manager that load images from web using http [client].
 *
 * @param client http client used for loading animation
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = io.github.alexzhirkevich.compottie.network.NetworkAssetsManager(client, cacheStrategy)
