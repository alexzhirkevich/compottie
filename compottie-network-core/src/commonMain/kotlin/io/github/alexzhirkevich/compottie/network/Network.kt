package io.github.alexzhirkevich.compottie.network

import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.MapMutex
import io.github.alexzhirkevich.compottie.ioDispatcher
import kotlinx.coroutines.withContext
import okio.Path

@OptIn(InternalCompottieApi::class)
private val NetworkLock = MapMutex()

@OptIn(InternalCompottieApi::class)
internal suspend fun networkLoad(
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy,
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

                val bytes = client.get(url)

                try {
                    cacheStrategy.save(url, bytes)?.let {
                        return@withLock it to bytes
                    }
                } catch (e: Throwable) {
                    Compottie.logger?.error(
                        "${this::class.simpleName} failed to cache downloaded asset",
                        e
                    )
                }
                null to bytes
            } catch (t: Throwable) {
                null to null
            }
        }
    }
}