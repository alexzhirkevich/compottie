package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TileMode.Companion.Clamp
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Shader

internal actual fun MakeLinearGradient(
    from : Offset,
    to : Offset,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode,
    matrix: Matrix
) = Shader.makeLinearGradient(
    x0 = from.x,
    y0 = from.y,
    x1 = to.x,
    y1 = to.y,
    colors = colors.toIntArray(),
    positions = colorStops.toFloatArray(),
    style = GradientStyle(
        tileMode = tileMode.toSkiaTileMode(),
        isPremul = true,
        localMatrix = matrix.asSkia33()
    )
)

internal actual fun MakeRadialGradient(
    center : Offset,
    radius : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode,
    matrix: Matrix
) = Shader.makeRadialGradient(
    x = center.x,
    y = center.y,
    r = radius,
    colors = colors.toIntArray(),
    positions = colorStops.toFloatArray(),
    style = GradientStyle(
        tileMode = tileMode.toSkiaTileMode(),
        isPremul = true,
        localMatrix = matrix.asSkia33()
    )
)


internal fun Matrix.asSkia33() : Matrix33 {
    return Matrix33(
        values[Matrix.ScaleX],
        values[Matrix.SkewX],
        values[Matrix.TranslateX],
        values[Matrix.SkewY],
        values[Matrix.ScaleY],
        values[Matrix.TranslateY],
        values[Matrix.Perspective0],
        values[Matrix.Perspective1],
        values[Matrix.Perspective2],
    )
}

private fun List<Color>.toIntArray(): IntArray =
    IntArray(size) { i -> this[i].toArgb() }

internal fun TileMode.toSkiaTileMode(): FilterTileMode = when (this) {
    Clamp -> FilterTileMode.CLAMP
    TileMode.Repeated -> FilterTileMode.REPEAT
    TileMode.Mirror -> FilterTileMode.MIRROR
    TileMode.Decal -> FilterTileMode.DECAL
    else -> FilterTileMode.CLAMP
}

//internal actual val ColorFilter.Companion.Luma get() = org.jetbrains.skia.ColorFilter.luma.asComposeColorFilter()



internal actual fun Paint.setBlurMaskFiler(radius: Float) {
    val skPaint = asFrameworkPaint()

    if (radius > 0f) {
        skPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, radius)
    } else {
        skPaint.maskFilter = null
    }
}
