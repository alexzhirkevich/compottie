package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.helpers.BooleanIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class VectorKeyframe(

    @SerialName("s")
    override val start : List<Float>? = null,

    @SerialName("e")
    override val end : List<Float>? = null,

    @SerialName("t")
    override val time : Float,

    @SerialName("h")
    @Serializable(with = BooleanIntSerializer::class)
    override val hold: Boolean = false,

    @SerialName("i")
    override val inValue : BezierInterpolation? = null,

    @SerialName("o")
    override val outValue : BezierInterpolation? = null,

    @SerialName("ti")
    val inTangent: List<Float>? = null,

    @SerialName("to")
    val outTangent: List<Float>? = null,
) : Keyframe<List<Float>> by BaseKeyframe(
    start = start,
    end = end,
    time = time,
    hold = hold,
    inValue = inValue,
    outValue = outValue
) {
    fun copy(): VectorKeyframe {
        return VectorKeyframe(
            start = start,
            end = end,
            time = time,
            hold = hold,
            inValue = inValue,
            outValue = outValue,
            inTangent = inTangent,
            outTangent = outTangent
        )
    }
}
