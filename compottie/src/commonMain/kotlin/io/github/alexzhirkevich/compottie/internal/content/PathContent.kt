package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.content.Content

interface PathContent : Content {
    fun getPath(time: Int) : Path
}