package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TileMode.Companion.Clamp
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.Shader


actual fun MakeLinearGradient(
    from : Offset,
    to : Offset,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode,
    matrix: Matrix
) =  Shader.makeLinearGradient(
    x0 = from.x,
    y0 = from.y,
    x1 = to.x,
    y1 = to.y,
    colors = colors.toIntArray(),
    positions = colorStops.toFloatArray(),
    style = GradientStyle(
        tileMode = tileMode.toSkiaTileMode(),
        isPremul = true,
        localMatrix = Matrix44(*matrix.values).asMatrix33()
    )
)

actual fun MakeRadialGradient(
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
        localMatrix = Matrix44(*matrix.values).asMatrix33()
    )
)

private fun List<Color>.toIntArray(): IntArray =
    IntArray(size) { i -> this[i].toArgb() }

internal fun TileMode.toSkiaTileMode(): FilterTileMode = when (this) {
    Clamp -> FilterTileMode.CLAMP
    TileMode.Repeated -> FilterTileMode.REPEAT
    TileMode.Mirror -> FilterTileMode.MIRROR
    TileMode.Decal -> FilterTileMode.DECAL
    else -> FilterTileMode.CLAMP
}
