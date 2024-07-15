package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import io.github.alexzhirkevich.compottie.avp.xml.AnimatedVectorProperty
import androidx.compose.ui.graphics.lerp as colorLerp


public class PaintData(
    public var color: Color = Color.Transparent,
    public var shader : Shader? = null
)

public sealed class PaintAnimator : ObjectAnimator<ColorData, PaintData>()

internal class DynamicPaintAnimator(
    override val duration: Float,
    override val valueFrom: ColorData,
    override val valueTo: ColorData,
    override val delay: Float,
    override val easing: Easing,
    override val property: AnimatedVectorProperty<PaintAnimator>
) : PaintAnimator() {

    private val paintData = PaintData()

    override fun interpolate(progress: Float): PaintData {

        paintData.color = Color.Transparent
        paintData.shader = null

        when {
            valueFrom is ColorData.Solid && valueTo is ColorData.Solid -> {
                paintData.color = colorLerp(valueFrom.color, valueTo.color, progress)
            }

            valueFrom is ColorData.GradientColorData
                    && valueTo is ColorData.GradientColorData
                    && valueTo::class == valueFrom::class -> {
                paintData.shader = valueFrom.lerpShader(valueTo, progress)
            }
        }

        return paintData
    }
}

internal class StaticPaintAnimator(
    val value : ColorData,
    override val property: AnimatedVectorProperty<PaintAnimator>
) : PaintAnimator() {

    override val delay: Float get() = 0f
    override val duration: Float get() = 0f
    override val valueFrom: ColorData get() = value
    override val valueTo: ColorData get() = value
    override val easing: Easing get() = LinearEasing

    private val paint = PaintData()

    override fun interpolate(progress: Float): PaintData {
        paint.color = Color.Transparent
        paint.shader = null
        when (value) {
            is ColorData.GradientColorData -> {
                paint.shader = value.shader
            }

            is ColorData.Solid -> {
                paint.color = value.color
            }
        }

        return paint
    }
}