package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap

internal actual suspend fun ImageBitmap.Companion.fromBytes(
    bytes: ByteArray
): ImageBitmap = bytes.decodeToImageBitmap()