package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.keyframes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class ValueKeyframe(

    @SerialName("s")
    val start: FloatArray = floatArrayOf(0f),

    @SerialName("t")
    override val time: Int,

    @SerialName("i")
    val inValue : BezierCurveInterpolation? = null,

    @SerialName("o")
    val outValue : BezierCurveInterpolation? = null,
) : Keyframe {

    @Transient
    internal val easing = if (inValue != null && outValue != null) {
        CubicBezierEasing(
            inValue.x[0],
            inValue.y[0],
            outValue.x[0],
            outValue.y[0]
        )
    } else null
}

internal fun Iterable<ValueKeyframe>.toAnimation() = keyframes<Float> {
    durationMillis = last().time
    forEach {
        if (it.easing != null) {
            it.start[0] at it.time using it.easing
        } else {
            it.start[0] at it.time
        }
    }
}.vectorize(
    converter = TwoWayConverter(
        convertToVector = {
            AnimationVector(it)
        },
        convertFromVector = { it.value }
    )
).createAnimation(
    initialValue = AnimationVector(first().start[0]),
    targetValue = AnimationVector(last().start[0]),
    initialVelocity = ZeroVector1d
)

private val ZeroVector1d = AnimationVector(0f)