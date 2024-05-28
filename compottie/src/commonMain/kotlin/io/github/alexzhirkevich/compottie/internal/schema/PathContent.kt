package io.github.alexzhirkevich.compottie.internal.schema

import androidx.compose.ui.graphics.Path

interface PathContent : Content {
    fun getPath(time: Int) : Path
}