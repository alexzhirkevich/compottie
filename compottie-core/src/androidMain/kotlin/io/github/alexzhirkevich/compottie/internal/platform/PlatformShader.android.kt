package io.github.alexzhirkevich.compottie.internal.platform

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.nativePaint
import io.github.alexzhirkevich.compottie.internal.utils.degreeToRadians
import kotlin.math.cos
import kotlin.math.sin


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
    tempMatrix.setFromInternal(matrix)
    setLocalMatrix(tempMatrix)
}

internal actual fun MakeRadialGradient(
    center : Offset,
    radius : Float,
    highlightingAngle : Float,
    highlightingLength : Float,
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
    tempMatrix.setFromInternal(matrix)
    if (highlightingLength != 0f) {
        val angle = degreeToRadians(highlightingAngle.toDouble())
        val focalOffsetX = (highlightingLength * sin(angle)).toFloat()
        val focalOffsetY = (highlightingLength  * cos(angle)).toFloat()
        tempMatrix.postTranslate(focalOffsetX, focalOffsetY)
    }
    setLocalMatrix(tempMatrix)
}

internal actual fun MakeSweepGradient(
    center: Offset,
    angle: Float,
    colors: List<Color>,
    colorStops: List<Float>,
    matrix: Matrix
): Shader = SweepGradientShader(
    center = center,
    colors = colors,
    colorStops = colorStops,
).apply {
    tempMatrix.setFromInternal(matrix)
    if (angle != 0f) {
        tempMatrix.postRotate(angle, center.x, center.y)
    }
    setLocalMatrix(tempMatrix)
}

internal actual fun Paint.setBlurMaskFilter(radius: Float, isImage : Boolean) {
    val fPaint = nativePaint

    if (radius > 0f) {
        fPaint.maskFilter = BlurMaskFilter(radius * BlurSigmaScale, BlurMaskFilter.Blur.NORMAL)
    } else {
        fPaint.maskFilter = null
    }
}

internal val BlurSigmaScale = .5f
