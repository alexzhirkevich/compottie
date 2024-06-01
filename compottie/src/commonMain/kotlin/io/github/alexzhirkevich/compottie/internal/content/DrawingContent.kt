package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope

internal interface DrawingContent : Content {
    fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, frame: Float)

    fun getBounds(outBounds: MutableRect, parentMatrix: Matrix, applyParents: Boolean, frame: Float)
}