package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toByteArray

/**
 * Asset manager that load images from web.
 *
 * It can't be used to download fonts. Combine it with other font-loading manager
 * using [LottieAssetsManager.combine] if needed
 * */
fun NetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
    request : NetworkRequest = GetRequest,
) : LottieAssetsManager = NetworkAssetsManagerImpl(
    client = client,
    cacheStrategy = cacheStrategy,
    request = request,
)

private class NetworkAssetsManagerImpl(
    private val client: HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
    private val request : NetworkRequest,
) : LottieAssetsManager {

    override suspend fun image(image: LottieImage): ImageRepresentable? {
        return try {
            val path = image.path + image.name

            val url = try {
                Url(path)
            } catch (t: URLParserException) {
                return null
            }

            try {
                cacheStrategy.load(path)?.let {
                    return ImageRepresentable.Bytes(it)
                }
            } catch (_: Throwable) { }

            val bytes = request(client, url).bodyAsChannel().toByteArray()

            try {
                cacheStrategy.save(path, bytes)
            } catch (e: Throwable) {
                L.logger.error("NetworkAssetsManager failed to cache downloaded asset", e)
            }
            ImageRepresentable.Bytes(bytes)
        } catch (t: Throwable) {
            null
        }
    }
}