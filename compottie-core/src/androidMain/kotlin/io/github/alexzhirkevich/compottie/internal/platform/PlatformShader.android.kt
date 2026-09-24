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
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toColorLong

private val _tmpMatrix = ThreadLocal<android.graphics.Matrix>()

internal actual fun MakeLinearGradient(
    from: Offset,
    to: Offset,
    colors: List<Color>,
    colorStops: List<Float>,
    matrix: Matrix
) = LinearGradientShader(
    from = from,
    to = to,
    colorStops = colorStops,
    colors = colors
).apply {
    val m = _tmpMatrix.getOrSet { android.graphics.Matrix() }
    m.setFromInternal(matrix)
    setLocalMatrix(m)
}

internal actual fun MakeRadialGradient(
    center : Offset,
    radius : Float,
    highlightingAngle : Float,
    highlightingLength : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    matrix: Matrix
) : Shader {
    val shader = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        && highlightingLength != 0f
        && radius > 0.01f
    ) {

        val focal = radialFocalPoint(center, radius, highlightingAngle, highlightingLength)
        val argbColors = LongArray(colors.size) { colors[it].toColorLong() }
        val positions = colorStops.toFloatArray()

        RadialGradient(
            focal.x,
            focal.y,
            0f,
            center.x,
            center.y,
            radius,
            argbColors,
            positions,
            android.graphics.Shader.TileMode.CLAMP
        )
    } else {
        RadialGradientShader(
            center = center,
            radius = radius,
            colorStops = colorStops,
            colors = colors
        )
    }

    val m = _tmpMatrix.getOrSet { android.graphics.Matrix() }
    m.setFromInternal(matrix)
    shader.setLocalMatrix(m)
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
    val m = _tmpMatrix.getOrSet { android.graphics.Matrix() }
    m.setFromInternal(matrix)
    if (angle != 0f) {
        m.postRotate(angle, center.x, center.y)
    }
    setLocalMatrix(m)
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
