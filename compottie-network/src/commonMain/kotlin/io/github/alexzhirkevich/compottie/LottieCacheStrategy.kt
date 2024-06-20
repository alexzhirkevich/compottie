package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable

@Stable
interface LottieCacheStrategy {

    suspend fun save(
        url: String,
        bytes: ByteArray
    )

    suspend fun load(
        url: String
    ): ByteArray?
}

