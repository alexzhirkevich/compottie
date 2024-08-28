@file:JvmName("CoreNetworkAssetsManager")


package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec
import kotlin.jvm.JvmName

/**
 * Asset manager that load images from web using [request] .
 *
 * @param request network request used for loading assets
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkAssetsManager(
    request : suspend (url: String) -> ByteArray,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    request = request,
    cacheStrategy = cacheStrategy,
)


@Stable
private class NetworkAssetsManagerImpl(
    private val request : suspend (url: String) -> ByteArray,
    private val cacheStrategy: LottieCacheStrategy,
) : LottieAssetsManager {

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {
        return networkLoad(
            request = request,
            cacheStrategy = cacheStrategy,
            url = image.path + image.name
        ).second?.let(ImageRepresentable::Bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkAssetsManagerImpl

        if (request != other.request) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}