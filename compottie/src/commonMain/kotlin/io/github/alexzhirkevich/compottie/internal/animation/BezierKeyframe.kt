package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.content.ShapeModifierContent
import io.github.alexzhirkevich.compottie.internal.content.modifiedBy
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.helpers.toShapeData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
internal class BezierKeyframe(

    @SerialName("s")
    @Serializable(with = BezierSerializer::class)
    override val start: Bezier? = null,

    @SerialName("e")
    @Serializable(with = BezierSerializer::class)
    override val end: Bezier? = null,

    @SerialName("t")
    override val time: Float,

    @SerialName("h")
    override val hold: BooleanInt = BooleanInt.No,

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
    outValue = outValue,
    hold = hold
)

internal class ShapeKeyframe(

    override val start: ShapeData? = null,

    override val end: ShapeData? = null,

    override val time: Float,

    override val hold: BooleanInt = BooleanInt.No,

    override val inValue : BezierInterpolation? = null,

    override val outValue : BezierInterpolation? = null,

) : Keyframe<ShapeData>()


internal class BezierSerializer : JsonTransformingSerializer<Bezier>(Bezier.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonArray){
            return element.first()
        }
        return element
    }
}