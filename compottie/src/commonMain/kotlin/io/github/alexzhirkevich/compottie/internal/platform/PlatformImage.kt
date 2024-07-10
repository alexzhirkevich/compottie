package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap

public expect fun ImageBitmap.Companion.fromBytes(bytes: ByteArray) : ImageBitmap