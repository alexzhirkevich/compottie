package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use

public actual fun ImageBitmap.Companion.fromBytes(bytes: ByteArray) : ImageBitmap {
    return Image.makeFromEncoded(bytes).use(Image::toComposeImageBitmap)
}