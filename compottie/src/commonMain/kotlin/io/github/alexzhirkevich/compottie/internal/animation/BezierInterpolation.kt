package io.github.alexzhirkevich.compottie.internal.animation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer


@Serializable
class BezierInterpolation(
    @Serializable(with = BezierCoordinateSerializer::class)
    val x : List<Float>,

    @Serializable(with = BezierCoordinateSerializer::class)
    val y : List<Float>
)

@OptIn(ExperimentalSerializationApi::class)
internal class BezierCoordinateSerializer : JsonTransformingSerializer<List<Float>>(ListSerializer(Float.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonPrimitive){
             JsonArray(listOf(element))
        } else element
    }
}

