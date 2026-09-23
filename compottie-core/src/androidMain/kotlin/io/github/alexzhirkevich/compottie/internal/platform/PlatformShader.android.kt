package io.github.alexzhirkevich.compottie.internal.platform

import android.graphics.BlurMaskFilter
import android.graphics.RadialGradient
import android.os.Build
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
import androidx.compose.ui.graphics.toAndroidTileMode
import androidx.compose.ui.graphics.toColorLong


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
) : Shader {

    val focal = radialFocalPoint(center, radius, highlightingAngle, highlightingLength)

    val argbColors = LongArray(colors.size) { colors[it].toColorLong() }
    val positions = colorStops.toFloatArray()
    val shader =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RadialGradient(
            focal.x,
            focal.y,
            0f,
            center.x,
            center.y,
            radius,
            argbColors,
            positions,
            tileMode.toAndroidTileMode()
        )
    } else {
        RadialGradientShader(
            center = center,
            radius = radius,
            colorStops = colorStops,
            tileMode = tileMode,
            colors = colors
        )
    }

    tempMatrix.setFromInternal(matrix)
    shader.setLocalMatrix(tempMatrix)
    return shader
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
