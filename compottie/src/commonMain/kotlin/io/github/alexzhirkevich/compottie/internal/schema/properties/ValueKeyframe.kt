package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.keyframes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class ValueKeyframe(

    @SerialName("s")
    override val start: FloatArray? = null,

    @SerialName("e")
    override val end: FloatArray? = null,

    @SerialName("t")
    override val time: Int,

    @SerialName("i")
    val inValue : BezierCurveInterpolation? = null,

    @SerialName("o")
    val outValue : BezierCurveInterpolation? = null,
) : Keyframe<FloatArray> {

    @Transient
    override val easing = if (inValue != null && outValue != null) {
        CubicBezierEasing(
            inValue.x[0],
            inValue.y[0],
            outValue.x[0],
            outValue.y[0]
        )
    } else LinearEasing
}



