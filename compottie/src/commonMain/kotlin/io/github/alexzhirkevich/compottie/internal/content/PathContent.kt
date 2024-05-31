package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.graphics.Path

internal interface PathContent : Content {
    fun getPath(frame: Float) : Path
}