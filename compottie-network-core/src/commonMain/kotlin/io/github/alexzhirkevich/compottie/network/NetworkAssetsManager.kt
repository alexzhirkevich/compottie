package io.github.alexzhirkevich.compottie.network

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec

/**
 * Asset manager that load images from web using [request] with [client].
 *
 * @param client http client used for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkAssetsManager(
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    client = client,
    cacheStrategy = cacheStrategy,
)


@Stable
private class NetworkAssetsManagerImpl(
    private val client: HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
) : LottieAssetsManager {

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {
        return networkLoad(
            client = client,
            cacheStrategy = cacheStrategy,
            url = image.path + image.name
        ).second?.let(ImageRepresentable::Bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkAssetsManagerImpl

        if (client != other.client) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = client.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}