package io.github.alexzhirkevich.compottie.internal.platform

import android.os.Build
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.setFrom

internal actual fun Canvas.getMatrix(matrix: Matrix) =
    matrix.setFrom(nativeCanvas.matrix)

internal actual fun Canvas.saveLayer(rect : MutableRect, paint : Paint, flag : Int)  {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        nativeCanvas.saveLayer(
            /* left = */ rect.left,
            /* top = */ rect.top,
            /* right = */ rect.right,
            /* bottom = */ rect.bottom,
            /* paint = */ paint.asFrameworkPaint(),
            /* saveFlags = */ flag
        )
    } else {
        nativeCanvas.saveLayer(
            /* left = */ rect.left,
            /* top = */rect.top,
            /* right = */rect.right,
            /* bottom = */rect.bottom,
            /* paint = */paint.asFrameworkPaint()
        )
    }
}
