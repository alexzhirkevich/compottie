package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toByteArray
import kotlinx.coroutines.withContext

/**
 * Asset manager that load images from web using [request] with [client].
 *
 * @param client http client used for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@Stable
fun NetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    client = client,
    cacheStrategy = cacheStrategy,
    request = request,
)



@Stable
private class NetworkAssetsManagerImpl(
    private val client: HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
    private val request : NetworkRequest,
) : LottieAssetsManager {

    override suspend fun image(image: LottieImage): ImageRepresentable? {
        return networkLoad(
            client = client,
            cacheStrategy = cacheStrategy,
            request = request,
            url = image.path + image.name
        ).second?.let(ImageRepresentable::Bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkAssetsManagerImpl

        if (client != other.client) return false
        if (cacheStrategy != other.cacheStrategy) return false
        if (request != other.request) return false

        return true
    }

    override fun hashCode(): Int {
        var result = client.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        result = 31 * result + request.hashCode()
        return result
    }
}