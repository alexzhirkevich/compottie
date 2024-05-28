package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorizedKeyframesSpec
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.keyframes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class VectorKeyframe(

    @SerialName("s")
    val start : FloatArray = floatArrayOf(0f,0f),

    @SerialName("t")
    val time : Int,

    @SerialName("i")
    val inValue : BezierCurveInterpolation? = null,

    @SerialName("o")
    val outValue : BezierCurveInterpolation? = null
) {

    @Transient
    val easing = if (inValue != null && outValue != null){
        CubicBezierEasing(
            outValue.x[0], outValue.y[0],
            inValue.x[0], inValue.y[0],
        )
    } else null
}



internal fun Iterable<VectorKeyframe>.to2DAnimation() = keyframes<FloatArray> {
    durationMillis = last().time

    forEach {
        if (it.easing != null) {
            it.start at it.time using it.easing
        } else {
            it.start at it.time
        }
    }
}.vectorize(
    converter = TwoWayConverter(
        convertToVector = {
            AnimationVector(it[0], it[1])
        },
        convertFromVector = {
            floatArrayOf(it.v1, it.v2)
        }
    )
).createAnimation(
    initialValue = AnimationVector(first().start[0], first().start[1]),
    targetValue =  AnimationVector(last().start[0], last().start[1]),
    initialVelocity = ZeroVector
)

private val ZeroVector = AnimationVector(0f,0f)


@Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")
private fun  KeyframesSpec<FloatArray>.vectorize(
): VectorizedKeyframesSpec<V> {

    @Suppress("PrimitiveInCollection") // Consumed by stable public API
    val vectorizedKeyframes = mutableMapOf<Int, Pair<V, Easing>>()
    config.keyframes.forEach { key, value ->
        vectorizedKeyframes[key] = value.toPair(converter.convertToVector)
    }
    return VectorizedKeyframesSpec(
        keyframes = vectorizedKeyframes,
        durationMillis = config.durationMillis,
        delayMillis = config.delayMillis
    )
}
