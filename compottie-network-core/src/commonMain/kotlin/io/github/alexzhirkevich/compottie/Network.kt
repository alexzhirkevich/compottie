package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.withContext
import okio.Path

@OptIn(InternalCompottieApi::class)
private val NetworkLock = MapMutex()

@OptIn(InternalCompottieApi::class)
internal suspend fun networkLoad(
    request : suspend (url: String) -> ByteArray,
    cacheStrategy: LottieCacheStrategy,
    url: String
): Pair<Path?, ByteArray?> {
    return withContext(Compottie.ioDispatcher()) {
        NetworkLock.withLock(url) {
            try {
                try {
                    cacheStrategy.load(url)?.let {
                        return@withLock cacheStrategy.path(url) to it
                    }
                } catch (_: Throwable) {
                }

                val bytes = request(url)

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