package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVectorN
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface EffectValue<T : RawProperty<Any>> : ExpressionHolder {

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

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
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

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
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
        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
    }

    @Serializable
    @SerialName("3")
    class Point(
        @SerialName("v")
        override val value: AnimatedVectorN? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedVectorN> {
        override fun copy() = Point(value?.copy(),name, index)

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
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

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
    }

    @Serializable
    @SerialName("7")
    class DropDown(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = DropDown(value?.copy(),name, index)

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
    }


    @Serializable
    @SerialName("10")
    class Layer(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = Layer(value?.copy(),name, index)

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
    }

    @Serializable
    class Unsupported : EffectValue<AnimatedVectorN> {
        override val name: String? = null
        override val index: Int? = null
        override val value: AnimatedVectorN? = null

        override fun copy() = Unsupported()
        override fun prepareExpressions(state: AnimationState) {

        }
    }
}