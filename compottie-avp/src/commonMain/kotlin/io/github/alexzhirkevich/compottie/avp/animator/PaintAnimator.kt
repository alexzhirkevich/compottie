package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp as colorLerp
import kotlin.math.min
import androidx.compose.ui.geometry.lerp as offsetLerp
import androidx.compose.ui.util.lerp as floatLerp

internal sealed interface ColorData {

    sealed interface GradientColorData : ColorData {
        val colorStops : List<Pair<Float, Color>>
    }

    class Solid(val color: Color) : ColorData

    class LinearGradient(
        override val colorStops : List<Pair<Float, Color>>,
        val start : Offset,
        val end : Offset,
        val tileMode: TileMode
    ) : GradientColorData {
        val colors = colorStops.map { it.second }
        val stops = colorStops.map { it.first }
    }
}


internal class PaintData(
    var color: Color = Color.Transparent,
    var shader : Shader? = null
)

internal sealed class PaintAnimator : ObjectAnimator<ColorData, PaintData>()

internal class DynamicPaintAnimator(
    override val duration: Float,
    override val valueFrom: ColorData,
    override val valueTo: ColorData,
    override val delay: Float,
    override val interpolator: Easing
) : ObjectAnimator<ColorData, PaintData>() {

    private val paintData = PaintData()

    private val colors = if (
        valueFrom is ColorData.GradientColorData &&
        valueTo is ColorData.GradientColorData
    ) {
        List(min(valueFrom.colorStops.size, valueTo.colorStops.size)) {
            Color.Transparent
        }.toMutableList()
    } else {
        mutableListOf()
    }
    private val colorStops = colors.map { 0f }.toMutableList()

    override fun interpolate(progress: Float): PaintData {

        paintData.color = Color.Transparent
        paintData.shader = null

        if (valueFrom is ColorData.Solid && valueTo is ColorData.Solid) {
            paintData.color = colorLerp(valueFrom.color, valueTo.color, progress)
        }
        if (valueFrom is ColorData.LinearGradient && valueTo is ColorData.LinearGradient) {

            repeat(colors.size) {
                colors[it] = colorLerp(
                    valueFrom.colors[it],
                    valueTo.colors[it],
                    progress
                )
                colorStops[it] = floatLerp(
                    valueFrom.stops[it],
                    valueTo.stops[it],
                    progress
                )
            }
            paintData.shader = LinearGradientShader(
                from = offsetLerp(valueFrom.start, valueTo.start, progress),
                to = offsetLerp(valueFrom.end, valueTo.end, progress),
                colors = colors,
                colorStops = colorStops,
                tileMode = valueTo.tileMode
            )
        }

        return paintData
    }
}

internal class StaticPaintAnimator(
    val value : ColorData
) : PaintAnimator(){

    override val delay: Float get() = 0f
    override val duration: Float get() = 0f
    override val valueFrom: ColorData get() = value
    override val valueTo: ColorData get() = value
    override val interpolator: Easing get() = LinearEasing

    private val paint = PaintData()
    override fun interpolate(progress: Float): PaintData {
        paint.color = Color.Transparent
        paint.shader = null
        when (value){
            is ColorData.LinearGradient -> {
                paint.shader = LinearGradientShader(
                    from = value.start,
                    to = value.end,
                    colors = value.colors,
                    colorStops = value.stops,
                    tileMode = value.tileMode
                )
            }
            is ColorData.Solid -> {
                paint.color = value.color
            }
        }

        return paint
    }
}