package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.lerp as colorLerp
import kotlin.math.min
import androidx.compose.ui.geometry.lerp as offsetLerp
import androidx.compose.ui.util.lerp as floatLerp

internal sealed interface ColorData {

    fun lerp(other : ColorData, progress: Float) :

    sealed interface GradientColorData : ColorData {
        val colorStops : List<Pair<Float, Color>>
    }

    class Solid(val color: Color) : ColorData {

    }

    class LinearGradient(
        override val colorStops : List<Pair<Float, Color>>,
        val from : Offset,
        val to : Offset
    ) : GradientColorData
}


internal class PaintData(
    var color: Color,
    var shader : Shader?
)

internal sealed class PaintAnimator : ObjectAnimator<ColorData, PaintData>()

internal class DynamicPaintAnimator(
    override val duration: Float,
    override val valueFrom: ColorData,
    override val valueTo: ColorData,
    override val startOffset: Float,
    override val interpolator: Easing
) : ObjectAnimator<ColorData, PaintData>() {

    private val paintData = PaintData(Color.Transparent, null)

    private val colors = if (valueFrom is ColorData.GradientColorData && valueTo is ColorData.GradientColorData)
        List(min(valueFrom.colorStops.size, valueTo.colorStops.size)){
            Color.Transparent
        }.toMutableList()
    else mutableListOf()

    private val colorStops = colors.map { 0f }.toMutableList()

    override fun interpolate(progress: Float): PaintData {

        paintData.color = Color.Transparent
        paintData.shader = null

        if (valueFrom is ColorData.Solid && valueTo is ColorData.Solid) {
            paintData.color = colorLerp(valueFrom.color, valueTo.color, progress)
        }
        if (valueFrom is ColorData.LinearGradient && valueTo is ColorData.LinearGradient) {

            val size = min(valueFrom.colorStops.size, valueTo.colorStops.size)
            paintData.shader = LinearGradientShader(
                from = offsetLerp(valueFrom.from, valueTo.from, progress),
                to = offsetLerp(valueFrom.to, valueTo.to, progress),
                colors = List(size) {
                    colorLerp(valueFrom.colorStops[it].second, valueTo.colorStops[it].second, progress)
                },
                colorStops = List(size) {
                    floatLerp(valueFrom.colorStops[it].first, valueTo.colorStops[it].first, progress)
                }
            )
        }

        return paintData
    }
}

internal class StaticPaintAnimator(
    override val startOffset: Float,
    override val duration: Float,
    override val valueFrom: ColorData,
    override val valueTo: ColorData,
    override val interpolator: Easing
) : PaintAnimator(){

}