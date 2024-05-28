package io.github.alexzhirkevich.compottie.internal.schema

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix

interface DrawableContent : Content {
    fun drawIntoCanvas(canvas: Canvas, parentMatrix: Matrix, time: Int)
}