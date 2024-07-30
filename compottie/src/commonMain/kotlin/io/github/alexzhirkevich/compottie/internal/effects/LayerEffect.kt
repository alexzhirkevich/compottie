package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanIntSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed class LayerEffect {

    abstract val enabled: Boolean
    abstract val name: String?
    abstract val index: Int?
    abstract val values: List<EffectValue<*>>

    val valueByName by lazy {
        values.associateBy { it.name.orEmpty() }
    }

    val valueByIndex by lazy {
        values.associateBy { it.index ?: Int.MIN_VALUE }
    }

    abstract fun copy(): LayerEffect

    @Serializable
    class UnsupportedEffect() : LayerEffect() {

//        @SerialName("ef")
        override val values: List<EffectValue<@Contextual RawProperty<@Contextual Any>>> = emptyList()

//        @SerialName("nm")
        override val name: String? = null

//        @SerialName("ix")
        override val index: Int? = null

//        @SerialName("en")
//        @Serializable(with = BooleanIntSerializer::class)
        override val enabled: Boolean = true

        override fun copy(): LayerEffect {
            return UnsupportedEffect(
//                values = values.map(EffectValue<RawProperty<*>>::copy),
//                name = name,
//                index = index,
//                enabled = enabled
            )
        }
    }
}