package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVectorN
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface EffectValue<T : RawProperty<Any>> {

    val value: T?

    val name : String?

    val index : Int?

    fun copy(): EffectValue<T>

    @Serializable
    @SerialName("0")
    class Slider(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {
        override fun copy() = Slider(value?.copy(),name, index)
    }

    @Serializable
    @SerialName("1")
    class Angle(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {
        override fun copy() = Angle(value?.copy(), name, index)
    }

    @Serializable
    @SerialName("4")
    class CheckBox(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {
        override fun copy() = CheckBox(value?.copy(),name, index)
    }

    @Serializable
    @SerialName("2")
    class Color(
        @SerialName("v")
        override val value: AnimatedColor? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedColor> {
        override fun copy() = Color(value?.copy(),name, index)
    }

    @Serializable
    class Unsupported(
        @SerialName("v")
        override val value: AnimatedVectorN? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedVectorN> {
        override fun copy() = Unsupported(value,name, index) // TODO deep copy JsonElement?
    }
}