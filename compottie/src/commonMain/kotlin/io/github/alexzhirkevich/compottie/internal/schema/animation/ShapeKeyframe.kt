package io.github.alexzhirkevich.compottie.internal.schema.animation

import io.github.alexzhirkevich.compottie.internal.schema.helpers.Bezier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class ShapeKeyframe(

    @SerialName("s")
    override val start: List<Bezier>? = null,

    @SerialName("e")
    override val end: List<Bezier>? = null,

    @SerialName("t")
    override val time: Int,

    @SerialName("i")
    override val inValue : BezierInterpolation? = null,

    @SerialName("o")
    override val outValue : BezierInterpolation? = null,
) : Keyframe<List<Bezier>>() {
}
