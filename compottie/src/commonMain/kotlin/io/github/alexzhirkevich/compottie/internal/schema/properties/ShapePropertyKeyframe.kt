package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal class ShapePropertyKeyframe(

    @SerialName("s")
    val start : List<ShapeProperty>,

    @SerialName("e")
    val end : List<ShapeProperty>,

    @SerialName("t")
    val time: Int,

    @SerialName("i")
    val inValue : BezierCurveInterpolation,

    @SerialName("o")
    val outValue : BezierCurveInterpolation,
)