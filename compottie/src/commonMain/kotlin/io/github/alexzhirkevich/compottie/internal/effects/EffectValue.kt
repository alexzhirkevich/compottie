package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface EffectValue {

    @Serializable
    @SerialName("0")
    class Slider(
        @SerialName("v")
        val value : AnimatedValue? = null
    ) : EffectValue

    @Serializable
    @SerialName("4")
    class CheckBox(
        @SerialName("v")
        val value : AnimatedValue? = null
    ) : EffectValue
}