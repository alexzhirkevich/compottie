package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVectorN
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.animation.expressions.toJs
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JSSymbol
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface EffectValue<T : RawProperty<Any>> : Callable, ExpressionHolder {

    val value: T?

    val name : String?

    val matchName : String?

    val index : Int?

    override suspend fun invoke(args: List<JsAny?>, runtime: ScriptRuntime): JsAny? {
        return when (args.getOrNull(0)?.toString()) {
            "0" -> value?.raw(runtime.state)?.toJs()
            else -> Undefined
        }
    }

    fun copy(): EffectValue<T>

    override fun toKotlin(runtime: ScriptRuntime) : Any = this

    override suspend fun bind(
        thisArg: JsAny?,
        args: List<JsAny?>,
        runtime: ScriptRuntime
    ): Callable = this

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when(property){
            JSSymbol.toPrimitive -> value?.raw(runtime.state)?.toJs()
            else -> super.get(property, runtime)
        }
    }

    @Serializable
    @SerialName("0")
    class Slider(
        @SerialName("v")
        override val value: AnimatedNumber? = null,
        @SerialName("nm")
        override val name: String? = null,
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = Slider(value?.copy(), name, matchName, index)

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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = Angle(value?.copy(), name, matchName, index)

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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedColor> {

        override suspend fun invoke(args: List<JsAny?>, runtime: ScriptRuntime): JsAny? {
            return when (args.getOrNull(0)?.toString()) {
                "0" -> value?.raw(runtime.state)?.red?.js
                "1" -> value?.raw(runtime.state)?.green?.js
                "2" -> value?.raw(runtime.state)?.blue?.js
                "3" -> value?.raw(runtime.state)?.alpha?.js
                else -> Undefined
            }
        }
        override fun copy() = Color(value?.copy(),name, matchName, index)
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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedVectorN> {

        override suspend fun invoke(args: List<JsAny?>, runtime: ScriptRuntime): JsAny? {
            return if (args.isEmpty()) {
                Undefined
            } else {
                value?.raw(runtime.state)?.getOrNull(runtime.toNumber(args[0]).toInt())?.js ?: Undefined
            }
        }

        override fun copy() = Point(value?.copy(),name, matchName, index)

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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = CheckBox(value?.copy(),name, matchName, index)

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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = DropDown(value?.copy(),name, matchName,index)

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
        @SerialName("mn")
        override val matchName: String? = null,
        @SerialName("ix")
        override val index: Int? = null,
    ) : EffectValue<AnimatedNumber> {

        override fun copy() = Layer(value?.copy(),name, matchName, index)

        override fun prepareExpressions(state: AnimationState) {
            value?.prepareExpressions(state)
        }
    }

    @Serializable
    class Unsupported : EffectValue<AnimatedVectorN> {
        override val name: String? = null
        override val index: Int? = null
        override val matchName: String? = null
        override val value: AnimatedVectorN? = null

        override fun copy() = Unsupported()
        override fun prepareExpressions(state: AnimationState) {

        }
    }
}