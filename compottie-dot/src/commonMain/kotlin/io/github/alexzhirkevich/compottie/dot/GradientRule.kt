@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedGradient
import io.github.alexzhirkevich.compottie.internal.animation.BezierInterpolation
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.VectorKeyframe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Gradient")
internal class GradientRule(
    override val id: String,
    override val animations: List<String>? = null,
    override val expression: String? = null,
    override val keyframes: List<GradientDotKeyframe>? = null,
    override val value: List<GradientStop>? = null
) : ThemeRule<List<GradientStop>> {

    override fun property(): RawProperty<*> {
        return when {
            value != null || expression != null -> AnimatedGradient.Default(
                colorsVector = value?.toGradientArray()
                    ?: floatArrayOf(0f,0f,0f,0f),
                expression = expression
            )
            keyframes != null -> AnimatedGradient.Animated(
                keyframes = keyframes.map { it.toCoreKeyframe() },
                expression = expression
            )
            else -> AnimatedGradient.Default(floatArrayOf(0f,0f,0f,0f))
        }.apply {
            numberOfColors = value?.size ?: keyframes?.maxOfOrNull { it.value.size } ?: 0
        }
    }
}

@Serializable
internal class GradientStop(
    val color : FloatArray,
    val offset : Float
)

@Serializable
internal class GradientDotKeyframe(
    override val frame: Float,
    override val value: List<GradientStop>,
    override val hold: Boolean = false,
    val inTangent : BezierInterpolation? = null,
    val outTangent : BezierInterpolation? = null,
) : DotKeyframe<List<GradientStop>> {

    override fun toCoreKeyframe(): VectorKeyframe {
        return VectorKeyframe(
            time = frame,
            start = value.toGradientArray(),
            hold = hold,
            inValue = inTangent,
            outValue = outTangent
        )
    }
}

private fun List<GradientStop>.toGradientArray() : FloatArray {

    return buildList(size * 5) {
        this@toGradientArray.forEach {
            add(it.offset)
            repeat(3) { idx ->
                add(it.color[idx])
            }
        }
        this@toGradientArray.forEach {
            add(it.offset)
            add(if (it.color.size == 4) it.color[3] else 1f)
        }
    }.toFloatArray()
}