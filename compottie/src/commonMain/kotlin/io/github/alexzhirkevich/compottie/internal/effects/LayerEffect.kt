package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface LayerEffect {

    val name : String?

    val index : Int?

    val values : List<EffectValue<*>>

    fun copy() : LayerEffect

    @Serializable
    class UnsupportedEffect(
        @SerialName("ef")
        override val values: List<EffectValue<@Contextual Any?>> = emptyList(),

        @SerialName("nm")
        override val name : String? = null,

        @SerialName("ix")
        override val index : Int? = null,
    ) : LayerEffect {

        override fun copy(): LayerEffect {
            return UnsupportedEffect()
        }
    }
}