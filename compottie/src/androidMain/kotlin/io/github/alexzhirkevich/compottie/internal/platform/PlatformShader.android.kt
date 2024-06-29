package io.github.alexzhirkevich.compottie.internal.platform

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.setFrom


private val tempMatrix = android.graphics.Matrix()

internal actual fun MakeLinearGradient(
    from : Offset,
    to : Offset,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode,
    matrix: Matrix
) = LinearGradientShader(
    from = from,
    to = to,
    colorStops = colorStops,
    tileMode = tileMode,
    colors = colors
).apply {
    tempMatrix.setFrom(matrix)
    setLocalMatrix(tempMatrix)
}

internal actual fun MakeRadialGradient(
    center : Offset,
    radius : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode,
    matrix: Matrix
)  = RadialGradientShader(
    center = center,
    radius = radius,
    colorStops = colorStops,
    tileMode = tileMode,
    colors = colors
).apply {
    tempMatrix.setFrom(matrix)
    setLocalMatrix(tempMatrix)
}

internal actual fun Paint.setBlurMaskFilter(radius: Float, isImage : Boolean) {
    val fPaint = asFrameworkPaint()

    if (radius > 0f) {
        fPaint.setMaskFilter(BlurMaskFilter(radius * BlurSigmaScale, BlurMaskFilter.Blur.NORMAL))
    } else {
        fPaint.setMaskFilter(null)
    }
}

internal val BlurSigmaScale = .5f
