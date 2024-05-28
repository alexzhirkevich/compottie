package com.airbnb.lottie.animation.content

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix

interface DrawingContent : Content {
    fun draw(canvas: Canvas?, parentMatrix: Matrix?, alpha: Int)

    fun getBounds(outBounds: Rect?, parentMatrix: Matrix?, applyParents: Boolean)
}
