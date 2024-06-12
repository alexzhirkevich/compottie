package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFont
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
                L.logger.error("Failed to load lottie asset ${image.id} - incorrect url", t)
                return null
            }

            cacheStrategy.load(path)?.let {
                return ImageRepresentable.Bytes(it)
            }

            val bytes = request(client, url).bodyAsChannel().toByteArray()
            cacheStrategy.save(path, bytes)
            ImageRepresentable.Bytes(bytes)
        } catch (t: Throwable) {
            null
        }
    }

    override suspend fun font(font: LottieFont): Font? = null
}