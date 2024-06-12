package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
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

internal class BezierSerializer : JsonTransformingSerializer<Bezier>(Bezier.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonArray){
            return element.first()
        }
        return element
    }
}