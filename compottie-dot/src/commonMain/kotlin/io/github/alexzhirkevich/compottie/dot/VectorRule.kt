@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.BezierInterpolation
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.VectorKeyframe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Vector")
internal class VectorRule(
    override val id: String,
    override val animations: List<String>? = null,
    override val expression: String? = null,
    override val keyframes: List<VectorDotKeyframe>? = null,
    override val value: FloatArray? = null
) : ThemeRule<FloatArray> {

    override fun property(): RawProperty<*> {
        return when {
            value != null || expression != null -> AnimatedVector2.Default(
                value = value ?: floatArrayOf(0f,0f,0f,0f),
                expression = expression
            )
            keyframes != null -> AnimatedVector2.Animated(
                keyframes = keyframes.map { it.toCoreKeyframe() },
                expression = expression
            )
            else -> AnimatedVector2.Default(floatArrayOf(0f,0f,0f,0f))
        }
    }
}

@Serializable
internal class VectorDotKeyframe(
    override val frame: Float,
    override val value: FloatArray,
    override val hold: Boolean = false,
    val inTangent : BezierInterpolation? = null,
    val outTangent : BezierInterpolation? = null,
) : DotKeyframe<FloatArray> {

    override fun toCoreKeyframe(): VectorKeyframe {
        return VectorKeyframe(
            time = frame,
            start = value,
            hold = hold,
            inValue = inTangent,
            outValue = outTangent
        )
    }

}