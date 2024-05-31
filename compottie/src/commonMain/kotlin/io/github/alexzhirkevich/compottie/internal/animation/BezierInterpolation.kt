package io.github.alexzhirkevich.compottie.internal.schema.animation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer


@Serializable
class BezierInterpolation(
    @Serializable(with = BezierCoordinateSerializer::class)
    val x : Array<Float>,

    @Serializable(with = BezierCoordinateSerializer::class)
    val y : Array<Float>
)

@OptIn(ExperimentalSerializationApi::class)
internal class BezierCoordinateSerializer : JsonTransformingSerializer<Array<Float>>(ArraySerializer(Float.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonPrimitive){
             JsonArray(listOf(element))
        } else element
    }
}

