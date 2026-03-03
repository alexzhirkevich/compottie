package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.BezierInterpolation
import io.github.alexzhirkevich.compottie.internal.animation.Keyframe
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.ValueKeyframe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Scalar")
internal class ScalarRule(
    override val id: String,
    override val animations: List<String>? = null,
    override val expression: String? = null,
    override val keyframes: List<ScalarDotKeyframe>? = null,
    override val value: Float? = null
) : ThemeRule<Float> {

    override fun property(): RawProperty<*> {
        return when {
            value != null || expression != null -> AnimatedNumber.Default(
                value = value ?: 0f,
                expression = expression
            )
            keyframes != null -> AnimatedNumber.Animated(
                keyframes = keyframes.map { it.toCoreKeyframe() },
                expression = expression
            )
            else -> AnimatedNumber.Default(0f)
        }
    }
}

@Serializable
internal class ScalarDotKeyframe(
    override val frame: Float,
    override val value: Float,
    override val hold: Boolean = false,
    val inTangent : BezierInterpolation? = null,
    val outTangent : BezierInterpolation? = null,
) : DotKeyframe<Float> {

    override fun toCoreKeyframe(): ValueKeyframe {
        return ValueKeyframe(
            time = frame,
            start = floatArrayOf(value),
            hold = hold,
            inValue = inTangent,
            outValue = outTangent
        )
    }

}