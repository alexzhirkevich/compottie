package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface ShapeProperties : Indexable {

    fun interpolated(time : Int) : ShapeProperty

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value : ShapeProperty,

        @SerialName("x")
        override val expression : String? = null,

        @SerialName("ix")
        override val index: String? = null,

    ) : ShapeProperties {
        override fun interpolated(time: Int): ShapeProperty {
            return value
        }
    }

    @SerialName("1")
    @Serializable
    class Keyframed(

        @SerialName("k")
        val value : ShapePropertyKeyframe,

        @SerialName("x")
        override val expression : String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("ti")
        val inTangent: FloatArray? = null,

        @SerialName("to")
        val outTangent: FloatArray? = null,
    ) : ShapeProperties {
        override fun interpolated(time: Int): ShapeProperty {
            TODO("Not yet implemented")
        }
    }
}