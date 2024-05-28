package com.airbnb.lottie.animation.content

import androidx.compose.ui.graphics.Path


interface PathContent : Content {
    fun getPath() : Path
}
