package io.github.alexzhirkevich.compottie

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.URLParserException
import io.ktor.http.Url
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.readByteArray
import okio.Path

@OptIn(InternalCompottieApi::class)
private val NetworkLock = MapMutex()

@OptIn(InternalCompottieApi::class)
internal suspend fun networkLoad(
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy,
    request : NetworkRequest,
    url: String
): Pair<Path?, ByteArray?> {
    return withContext(ioDispatcher()) {
        NetworkLock.withLock(url) {
            try {
                try {
                    cacheStrategy.load(url)?.let {
                        return@withLock cacheStrategy.path(url) to it
                    }
                } catch (_: Throwable) {
                }

                val ktorUrl = try {
                    Url(url)
                } catch (t: URLParserException) {
                    Compottie.logger?.error("Failed to create URL $url", t)
                    return@withLock null to null
                }

                val source = request(client, ktorUrl).execute().body<Source>()
                val bytes = try {
                    source.readByteArray()
                } finally {
                    source.close()
                }

                try {
                    cacheStrategy.save(url, bytes)?.let {
                        return@withLock it to bytes
                    }
                } catch (e: Throwable) {
                    Compottie.logger?.error("Failed to cache downloaded $url", e)
                }
                null to bytes
            } catch (t: Throwable) {
                Compottie.logger?.error("Failed to download $url", t)
                null to null
            }
        }
    }
}