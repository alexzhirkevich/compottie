package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable

@Stable
public interface FileLoader {

    /**
     * Returns bytes of the file that was downloaded from [url]
     * */
    public suspend fun load(url: String): ByteArray?
}
