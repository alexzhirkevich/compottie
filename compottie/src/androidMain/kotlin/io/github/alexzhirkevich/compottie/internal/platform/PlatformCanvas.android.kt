package io.github.alexzhirkevich.compottie.internal.platform

import android.os.Build
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import io.github.alexzhirkevich.compottie.internal.utils.fastSetFrom
import androidx.compose.ui.graphics.setFrom

// the same as  androidx.compose.ui.graphics.setFrom but without arbitraty check
internal fun android.graphics.Matrix.setFromInternal(matrix: Matrix) {

    // We'll reuse the array used in Matrix to avoid allocation by temporarily
    // setting it to the 3x3 matrix used by android.graphics.Matrix
    // Store the values of the 4 x 4 matrix into temporary variables
    // to be reset after the 3 x 3 matrix is configured
    val scaleX = matrix.values[Matrix.ScaleX] // 0
    val skewY = matrix.values[Matrix.SkewY] // 1
    val v2 = matrix.values[2] // 2
    val persp0 = matrix.values[Matrix.Perspective0] // 3
    val skewX = matrix.values[Matrix.SkewX] // 4
    val scaleY = matrix.values[Matrix.ScaleY] // 5
    val v6 = matrix.values[6] // 6
    val persp1 = matrix.values[Matrix.Perspective1] // 7
    val v8 = matrix.values[8] // 8

    val translateX = matrix.values[Matrix.TranslateX]
    val translateY = matrix.values[Matrix.TranslateY]
    val persp2 = matrix.values[Matrix.Perspective2]

    val v = matrix.values

    v[android.graphics.Matrix.MSCALE_X] = scaleX
    v[android.graphics.Matrix.MSKEW_X] = skewX
    v[android.graphics.Matrix.MTRANS_X] = translateX
    v[android.graphics.Matrix.MSKEW_Y] = skewY
    v[android.graphics.Matrix.MSCALE_Y] = scaleY
    v[android.graphics.Matrix.MTRANS_Y] = translateY
    v[android.graphics.Matrix.MPERSP_0] = persp0
    v[android.graphics.Matrix.MPERSP_1] = persp1
    v[android.graphics.Matrix.MPERSP_2] = persp2

    setValues(v)

    // Reset the values back after the android.graphics.Matrix is configured
    v[Matrix.ScaleX] = scaleX // 0
    v[Matrix.SkewY] = skewY // 1
    v[2] = v2 // 2
    v[Matrix.Perspective0] = persp0 // 3
    v[Matrix.SkewX] = skewX // 4
    v[Matrix.ScaleY] = scaleY // 5
    v[6] = v6 // 6
    v[Matrix.Perspective1] = persp1 // 7
    v[8] = v8 // 8
}

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
