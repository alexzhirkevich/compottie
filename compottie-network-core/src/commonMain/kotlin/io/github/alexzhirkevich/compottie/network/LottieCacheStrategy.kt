package io.github.alexzhirkevich.compottie.network

import androidx.compose.runtime.Stable
import okio.Path

@Stable
public interface LottieCacheStrategy {

    /**
     * Returns path to the cached file that was downloaded from [url]
     * */
    public fun path(url: String) : Path?

    /**
     * Saves [bytes] downloaded from [url] to cache.
     * Returns path to the saved file
     * */
    public suspend fun save(
        url: String,
        bytes: ByteArray
    ): Path?

    /**
     * Loads bytes downloaded from [url] from cache
     * */
    public suspend fun load(
        url: String
    ): ByteArray?

    /**
     * Clear all ache
     * */
    public suspend fun clear()
}

