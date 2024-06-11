package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface EffectValue<T> {

    val value : T?

    @Serializable
    @SerialName("0")
    class Slider(
        @SerialName("v")
        override val value : AnimatedNumber? = null
    ) : EffectValue<AnimatedNumber>

    @Serializable
    @SerialName("1")
    class Angle(
        @SerialName("v")
        override val value : AnimatedNumber? = null
    ) : EffectValue<AnimatedNumber>

    @Serializable
    @SerialName("4")
    class CheckBox(
        @SerialName("v")
        override val value : AnimatedNumber? = null
    ) : EffectValue<AnimatedNumber>

    @Serializable
    @SerialName("2")
    class Color(
        @SerialName("v")
        override val value : AnimatedColor? = null
    ) : EffectValue<AnimatedColor>

    @Serializable
    class Unsupported(
        @SerialName("v")
        override val value : JsonElement? = null
    ) : EffectValue<JsonElement>
}