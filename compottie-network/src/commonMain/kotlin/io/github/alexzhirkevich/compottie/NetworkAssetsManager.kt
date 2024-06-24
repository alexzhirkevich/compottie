package io.github.alexzhirkevich.compottie

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
 * */
fun NetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    client = client,
    cacheStrategy = cacheStrategy,
    request = request,
)

private class NetworkAssetsManagerImpl(
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy,
    request : NetworkRequest,
) : NetworkDownloadManager(client, cacheStrategy, request), LottieAssetsManager {
    override suspend fun image(image: LottieImage): ImageRepresentable? {
        return load(image.path + image.name).second?.let(ImageRepresentable::Bytes)
    }
}