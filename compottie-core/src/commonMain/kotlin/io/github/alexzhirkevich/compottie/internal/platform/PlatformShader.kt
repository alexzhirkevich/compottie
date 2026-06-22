package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import io.github.alexzhirkevich.compottie.LruMap
import io.github.alexzhirkevich.compottie.dynamic.LottieGradient
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.helpers.GradientColors
import io.github.alexzhirkevich.compottie.internal.helpers.GradientType
import kotlin.math.hypot

internal class GradientCache {

    private val linear = LruMap<Shader>(limit = 10)
    private val radial = LruMap<Shader>(limit = 10)

    fun getOrPut(
        hash: Int,
        linear: Boolean,
        factory: () -> Shader
    ): Shader {
        val map = if (linear) this.linear else this.radial

        return map.getOrPut(hash, factory)
    }
}

internal fun GradientShader(
    gradient: LottieGradient,
    matrix: Matrix,
    cache: GradientCache
): Shader {

    return when (gradient){
        is LottieGradient.Linear -> {
            CachedLinearGradient(
                from = gradient.start,
                to = gradient.end,
                colors = gradient.colors,
                colorStops = gradient.stops,
                matrix = matrix,
                cache = cache
            )
        }
        is LottieGradient.Radial ->  {
            CachedRadialGradient(
                center = gradient.center,
                radius = gradient.radius,
                colors = gradient.colors,
                colorStops = gradient.stops,
                matrix = matrix,
                cache = cache
            )
        }

        is LottieGradient.Conic -> CachedSweepGradient(
            center = gradient.center,
            angle = gradient.angle,
            colors = gradient.colors,
            colorStops = gradient.stops,
            matrix = matrix,
            cache = cache
        )
    }
}

internal fun GradientShader(
    type: GradientType,
    startPoint: AnimatedVector2,
    endPoint: AnimatedVector2,
    colors: GradientColors,
    state: AnimationState,
    matrix: Matrix,
    cache: GradientCache
) : Shader {

    val start = Vec2(startPoint.interpolatedVec(state))
    val end = Vec2(endPoint.interpolatedVec(state))

    val c = colors.interpolated(state)

    return when (type) {
        GradientType.Linear -> {
            CachedLinearGradient(
                from = Offset(start.x, start.y),
                to = Offset(end.x, end.y),
                colors = c.colors,
                colorStops = c.colorStops,
                tileMode = TileMode.Clamp,
                matrix = matrix,
                cache = cache
            )
        }
        GradientType.Radial -> {
            val r = hypot((end.x - start.x), (end.y - start.y))

            CachedRadialGradient(
                radius = r,
                center = Offset(start.x, start.y),
                colors = c.colors,
                colorStops = c.colorStops,
                tileMode = TileMode.Clamp,
                matrix = matrix,
                cache = cache
            )
        }

        GradientType.Conic -> {
            CachedSweepGradient(
                angle = 0f,
                center = Offset(start.x, start.y),
                colors = c.colors,
                colorStops = c.colorStops,
                matrix = matrix,
                cache = cache
            )
        }
        else -> error("Unknown gradient type: $type")
    }
}

private fun CachedLinearGradient(
    from : Offset,
    to : Offset,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode = TileMode.Clamp,
    matrix: Matrix,
    cache : GradientCache,
) : Shader {

    var hash = from.hashCode()
    hash = (hash * 31) + to.hashCode()
    hash = (hash * 31) + colors.hashCode()
    hash = (hash * 31) + tileMode.hashCode()
    hash = (hash * 31) + matrix.hashCode()

    return cache.getOrPut(hash, true) {
        MakeLinearGradient(from, to, colors, colorStops, tileMode, matrix)
    }
}

private fun CachedRadialGradient(
    center : Offset,
    radius : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode = TileMode.Clamp,
    matrix: Matrix,
    cache : GradientCache,
) : Shader {

    var hash = center.hashCode()
    hash = (hash * 31) + radius.hashCode()
    hash = (hash * 31) + colors.hashCode()
    hash = (hash * 31) + tileMode.hashCode()
    hash = (hash * 31) + matrix.hashCode()

    return cache.getOrPut(hash, false) {
        MakeRadialGradient(center, radius, colors, colorStops, tileMode, matrix)
    }

}

private fun CachedSweepGradient(
    center : Offset,
    angle : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    matrix: Matrix,
    cache : GradientCache,
) : Shader {

    var hash = center.hashCode()
    hash = (hash * 31) + angle.hashCode()
    hash = (hash * 31) + colors.hashCode()
    hash = (hash * 31) + matrix.hashCode()

    return cache.getOrPut(hash, false) {
        MakeSweepGradient(center, angle, colors, colorStops, matrix)
    }
}



internal expect fun MakeLinearGradient(
    from : Offset,
    to : Offset,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode = TileMode.Clamp,
    matrix: Matrix
) : Shader


internal expect fun MakeRadialGradient(
    center : Offset,
    radius : Float,
    colors : List<Color>,
    colorStops: List<Float>,
    tileMode: TileMode = TileMode.Clamp,
    matrix: Matrix
) : Shader

internal expect fun MakeSweepGradient(
    center: Offset,
    angle: Float,
    colors: List<Color>,
    colorStops: List<Float>,
    matrix: Matrix
) : Shader

internal expect fun Paint.setBlurMaskFilter(radius: Float, isImage : Boolean = false)


internal val ColorFilter.Companion.Luma : ColorFilter
    get() = LumaColorFilter//org.jetbrains.skia.ColorFilter.luma.asComposeColorFilter()


private val LumaColorFilter by lazy {
    ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0.2126f, 0.7152f, 0.0722f, 0f, 0f
            )
        )
    )
}
