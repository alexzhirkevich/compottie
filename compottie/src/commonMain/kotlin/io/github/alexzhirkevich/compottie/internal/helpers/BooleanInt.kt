package io.github.alexzhirkevich.compottie.internal.helpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.intOrNull
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
internal value class BooleanInt(val value : Byte) {

    companion object {
        val Yes = BooleanInt(1)
        val No = BooleanInt(0)
    }

    fun toBoolean() = this == Yes
}

//TODO: replace all boolean ints with this serializer
internal class BooleanIntSerializer : JsonTransformingSerializer<Boolean>(Boolean.serializer()){
    override fun transformDeserialize(element: JsonElement): JsonElement {

        val int = (element as JsonPrimitive).intOrNull

        return if (int != null){
            JsonPrimitive(int == 1)
        } else {
            element
        }
    }
}