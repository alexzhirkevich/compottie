package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface LayerEffect {

    val values : List<EffectValue<*>>

    @Serializable
    data object UnsupportedEffect : LayerEffect {
        override val values: List<EffectValue<*>>
            get() = emptyList()
    }
}