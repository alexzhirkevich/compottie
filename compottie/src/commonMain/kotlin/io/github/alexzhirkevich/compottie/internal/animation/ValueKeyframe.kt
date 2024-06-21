package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
internal class ValueKeyframe(

    @SerialName("s")
    override val start: FloatArray? = null,

    @SerialName("e")
    override val end: FloatArray? = null,

    @SerialName("t")
    override val time: Float,

    @SerialName("h")
    override val hold: BooleanInt = BooleanInt.No,

    @SerialName("i")
    override val inValue : BezierInterpolation? = null,

    @SerialName("o")
    override val outValue : BezierInterpolation? = null,
) : Keyframe<FloatArray> by BaseKeyframe(
    start = start,
    end = end,
    time = time,
    hold = hold,
    inValue = inValue,
    outValue = outValue
)

