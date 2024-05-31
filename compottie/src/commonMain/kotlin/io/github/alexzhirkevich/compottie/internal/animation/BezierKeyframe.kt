package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.content.ShapeModifierContent
import io.github.alexzhirkevich.compottie.internal.content.modifiedBy
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.helpers.toShapeData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class BezierKeyframe(

    @SerialName("s")
    override val start: Bezier? = null,

    @SerialName("e")
    override val end: Bezier? = null,

    @SerialName("t")
    override val time: Float,

    @SerialName("i")
    override val inValue : BezierInterpolation? = null,

    @SerialName("o")
    override val outValue : BezierInterpolation? = null,
) : Keyframe<Bezier>()


internal fun BezierKeyframe.toShapeKeyframe(
    modifiers: List<ShapeModifierContent> = emptyList(), frame: Float = 0f
) = ShapeKeyframe(
    start = start?.toShapeData()?.modifiedBy(modifiers, frame),
    end = end?.toShapeData()?.modifiedBy(modifiers, frame),
    time = time,
    inValue = inValue,
    outValue = outValue
)

internal class ShapeKeyframe(

    override val start: ShapeData? = null,

    override val end: ShapeData? = null,

    override val time: Float,

    override val inValue : BezierInterpolation? = null,

    override val outValue : BezierInterpolation? = null,
) : Keyframe<ShapeData>()