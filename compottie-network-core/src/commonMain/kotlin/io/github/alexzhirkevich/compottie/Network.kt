package io.github.alexzhirkevich.compottie

import okio.Path

internal suspend fun networkLoad(
    request : suspend (url: String) -> ByteArray,
    cacheStrategy: LottieCacheStrategy,
    url: String
): Pair<Path?, ByteArray?> {
    return try {
        try {
            cacheStrategy.load(url)?.let {
                return cacheStrategy.path(url) to it
            }
        } catch (_: Throwable) {
        }

        val bytes = request(url)

        try {
            cacheStrategy.save(url, bytes)?.let {
                return it to bytes
            }
        } catch (e: Throwable) {
            Compottie.logger?.error(
                "Failed to cache downloaded asset",
                e
            )
        }
        null to bytes
    } catch (t: Throwable) {
        null to null
    }
}