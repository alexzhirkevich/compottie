package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import okio.Path

@Stable
interface LottieCacheStrategy {

    /**
     * Returns path to the cached file that was downloaded from [url]
     * */
    fun path(url: String) : Path?

    /**
     * Saves [bytes] downloaded from [url] to cache.
     * Returns path to the saved file
     * */
    suspend fun save(
        url: String,
        bytes: ByteArray
    ): Path?

    /**
     * Loads bytes downloaded from [url] from cache
     * */
    suspend fun load(
        url: String
    ): ByteArray?

    /**
     * Clear all ache
     * */
    suspend fun clear()
}

