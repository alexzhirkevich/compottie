package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

public actual fun ImageBitmap.Companion.fromBytes(bytes: ByteArray) : ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}