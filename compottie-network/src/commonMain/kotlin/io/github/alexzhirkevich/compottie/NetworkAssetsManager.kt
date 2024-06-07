package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.alexzhirkevich.compottie.assets.LottieAsset
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toByteArray

@Composable
fun rememberNetworkAssetsManager(
    client: HttpClient = DefaultHttpClient,
    cacheStrategy: LottieCacheStrategy = rememberDiskCacheStrategy(),
    request : NetworkRequest = GetRequest,
) {
    val updatedRequest by rememberUpdatedState(request)

    return remember(client, cacheStrategy) {
        NetworkAssetsManager(client, cacheStrategy) { c, u ->
            updatedRequest.invoke(c, u)
        }
    }
}

class NetworkAssetsManager(
    private val client: HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
    private val request : NetworkRequest,
) : LottieAssetsManager {

    override suspend fun fetch(asset: LottieAsset): ByteArray? {
        return try {
            val path = asset.path + asset.name

            val url = try {
                Url(path)
            } catch (t: URLParserException) {
                L.logger.error("Failed to load lottie asset ${asset.id} - incorrect url", t)
                return null
            }

            cacheStrategy.load(path)?.let {
                return it
            }

            request(client, url).bodyAsChannel().toByteArray().also {
                cacheStrategy.save(path, it)
            }
        } catch (t: Throwable) {
            null
        }
    }
}