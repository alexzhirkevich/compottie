package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface LayerEffect {

    @Serializable
    data object UnsupportedEffect : LayerEffect
}