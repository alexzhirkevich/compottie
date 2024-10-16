package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LruMap
import io.github.alexzhirkevich.compottie.dynamic.LottieGradient
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.helpers.GradientColors
import io.github.alexzhirkevich.compottie.internal.helpers.GradientType
import kotlin.math.hypot

@OptIn(ExperimentalCompottieApi::class)
internal class GradientCache {

    private val linear = LruMap<Shader>(limit = Compottie::shaderCacheLimit)
    private val radial = LruMap<Shader>(limit = Compottie::shaderCacheLimit)

    fun getOrPut(
        hash : Int,
        linear : Boolean,
        factory : () -> Shader
    ) : Shader {
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
                colors = gradient.colorStops.fastMap { it.second },
                colorStops = gradient.colorStops.fastMap { it.first },
                matrix = matrix,
                cache = cache
            )
        }
        is LottieGradient.Radial ->  {
            CachedRadialGradient(
                center = gradient.center,
                radius = gradient.radius,
                colors = gradient.colorStops.fastMap { it.second },
                colorStops = gradient.colorStops.fastMap { it.first },
                matrix = matrix,
                cache = cache
            )
        }
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

    val start = startPoint.interpolated(state)
    val end = endPoint.interpolated(state)

    colors.colors.numberOfColors = colors.numberOfColors

    val c = colors.colors.interpolated(state)

    return if (type == GradientType.Linear) {
        CachedLinearGradient(
            from = Offset(start.x, start.y),
            to = Offset(end.x, end.y),
            colors = c.colors,
            colorStops = c.colorStops,
            tileMode = TileMode.Clamp,
            matrix = matrix,
            cache = cache
        )
    } else {
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
