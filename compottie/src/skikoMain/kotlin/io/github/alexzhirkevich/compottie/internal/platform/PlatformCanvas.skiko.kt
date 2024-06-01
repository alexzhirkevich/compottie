package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas

internal actual fun Canvas.getMatrix(matrix: Matrix) {
    nativeCanvas.localToDevice.mat.copyInto(matrix.values)
}

internal actual fun Canvas.saveLayer(rect : MutableRect, paint : Paint, flag : Int) {
    nativeCanvas.saveLayer(
        left = rect.left,
        top = rect.top,
        right = rect.right,
        bottom = rect.bottom,
        paint = paint.asFrameworkPaint()
    )
}