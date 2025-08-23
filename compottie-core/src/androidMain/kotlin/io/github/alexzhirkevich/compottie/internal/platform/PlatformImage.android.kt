package io.github.alexzhirkevich.compottie.internal.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.Font

public actual fun ImageBitmap.Companion.fromBytes(bytes: ByteArray) : ImageBitmap {
    return BitmapFactory
        .decodeByteArray(bytes, 0, bytes.size)
        .asImageBitmap()
}

