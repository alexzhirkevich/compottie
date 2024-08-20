package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable

@Stable
public interface FileLoader {
    public suspend fun load(url: String): ByteArray?
}
