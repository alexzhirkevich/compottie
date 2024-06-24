package io.github.alexzhirkevich.compottie

import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toByteArray
import kotlinx.coroutines.withContext
import okio.Path

internal abstract class NetworkDownloadManager(
    private val client: HttpClient,
    private val cacheStrategy: LottieCacheStrategy,
    private val request : NetworkRequest,
) {

    @OptIn(InternalCompottieApi::class)
    protected suspend fun load(url: String): Pair<Path?, ByteArray?> {
        return withContext(ioDispatcher()) {
            try {
                try {
                    cacheStrategy.load(url)?.let {
                        return@withContext cacheStrategy.path(url) to it
                    }
                } catch (_: Throwable) {
                }

                val ktorUrl = try {
                    Url(url)
                } catch (t: URLParserException) {
                    return@withContext null to null
                }

                val bytes = request(client, ktorUrl).execute().bodyAsChannel().toByteArray()

                try {
                    cacheStrategy.save(url, bytes)?.let {
                        return@withContext it to bytes
                    }
                } catch (e: Throwable) {
                    L.logger.error("${this::class.simpleName} failed to cache downloaded asset", e)
                }
                null to bytes
            } catch (t: Throwable) {
                null to null
            }
        }
    }
}