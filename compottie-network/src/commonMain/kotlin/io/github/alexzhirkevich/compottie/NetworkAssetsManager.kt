package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.ktor.client.HttpClient

/**
 * Asset manager that load images from web using [request] with [client].
 *
 * @param client http client used for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@Stable
@Deprecated(
    "Use FileLoader instead of HttpClient",
    replaceWith = ReplaceWith("NetworkAssetsManager(fileLoader, cacheStrategy)")
)
public fun NetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    fileLoader = KtorFileLoader(client, request),
    cacheStrategy = cacheStrategy
)

/**
 * Asset manager that load images from web using [fileLoader].
 *
 * @param fileLoader loader used for loading animation
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@Stable
public fun NetworkAssetsManager(
    fileLoader: FileLoader = DefaultFileLoader,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    fileLoader = fileLoader,
    cacheStrategy = cacheStrategy
)

@Stable
private class NetworkAssetsManagerImpl(
    private val fileLoader: FileLoader,
    private val cacheStrategy: LottieCacheStrategy
) : LottieAssetsManager {

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {
        return networkLoad(
            fileLoader = fileLoader,
            cacheStrategy = cacheStrategy,
            url = image.path + image.name
        ).second?.let(ImageRepresentable::Bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkAssetsManagerImpl

        if (fileLoader != other.fileLoader) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileLoader.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}