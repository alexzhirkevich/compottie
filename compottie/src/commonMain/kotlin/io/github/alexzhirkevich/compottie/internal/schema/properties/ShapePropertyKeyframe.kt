package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal class ShapePropertyKeyframe(

    @SerialName("s")
    val start : List<ShapeProperty>,

    @SerialName("t")
    override val time: Int,

    @SerialName("i")
    val inValue : BezierCurveInterpolation,

    @SerialName("o")
    val outValue : BezierCurveInterpolation,
) : Keyframe