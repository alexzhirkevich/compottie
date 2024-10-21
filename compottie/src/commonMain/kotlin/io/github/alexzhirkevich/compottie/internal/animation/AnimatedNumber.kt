package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.invoke
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.isNotNull
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = AnimatedNumberSerializer::class)
internal sealed class AnimatedNumber : DynamicProperty<Float>() {

    abstract fun copy(): AnimatedNumber

    override fun mapEvaluated(e: Any): Float {
        return when (e) {
            is Number -> e.toFloat()
            is List<*> -> (e[0] as Number).toFloat()
            else -> error("Failed to cast $e to number")
        }
    }

    @Serializable
    class Default(
        @SerialName("k")
        @Serializable(with = ValueSerializer::class)
        val value: Float,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,
    ) : AnimatedNumber() {

        init {
            prepare()
        }

        override fun copy(): AnimatedNumber {
            return Default(
                value = value,
                expression = expression,
                index = index
            )
        }

        override fun raw(state: AnimationState): Float = value
    }

    @Serializable
    class Animated(
        @SerialName("k")
        override val keyframes: List<ValueKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,
    ) : AnimatedNumber(), AnimatedKeyframeProperty<Float, ValueKeyframe> {

        init {
            prepare()
        }

        @Transient
        private val delegate = BaseKeyframeAnimation(
            index = index,
            keyframes = keyframes,
            emptyValue = 1f,
            map = { s, e, p ->
                lerp(s[0], e[0], easingX.transform(p))
            }
        )

        override fun copy(): AnimatedNumber {
            return Animated(
                keyframes = keyframes,
                expression = expression,
                index = index
            )
        }

        override fun raw(state: AnimationState): Float {
            return delegate.raw(state)
        }
    }

    @Serializable
    class Slottable(
        val sid: String,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,
    ) : AnimatedNumber() {

        override fun copy(): AnimatedNumber {
            return Slottable(
                sid = sid,
                expression = expression,
                index = index
            )
        }

        override fun raw(state: AnimationState): Float {
            return state.composition.animation.slots.number(sid)?.interpolated(state) ?: 0f
        }
    }
}

internal fun AnimatedNumber.dynamicNorm(provider: PropertyProvider<Float>?) {
    dynamic = if (provider != null) PropertyProvider {
        provider.invoke(this, it) * 100f
    } else null
}

internal fun AnimatedNumber.Companion.defaultRotation() : AnimatedNumber =
    AnimatedNumber.Default(0f)

internal fun AnimatedNumber.Companion.defaultSkew() : AnimatedNumber =
    AnimatedNumber.Default(0f)

internal fun AnimatedNumber.Companion.defaultSkewAxis() : AnimatedNumber =
    AnimatedNumber.Default(0f)

internal fun AnimatedNumber.Companion.defaultOpacity() : AnimatedNumber =
    AnimatedNumber.Default(100f)

internal fun AnimatedNumber.Companion.defaultRoundness() : AnimatedNumber =
    AnimatedNumber.Default(0f)

internal fun AnimatedNumber.Companion.defaultRadius() : AnimatedNumber =
    AnimatedNumber.Default(0f)


internal fun AnimatedNumber.interpolatedNorm(state: AnimationState) = interpolated(state) / 100f

internal object ValueSerializer : JsonTransformingSerializer<Float>(Float.serializer()){
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when(element){
            is JsonArray -> element[0]
            else -> element
        }
    }
}

internal object AnimatedNumberSerializer : JsonContentPolymorphicSerializer<AnimatedNumber>(AnimatedNumber::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedNumber> {

        if (element is JsonPrimitive) {
            return AnimatedNumberAsPrimitiveSerializer
        }

        if (element is JsonObject && element["sid"].isNotNull()) {
            return AnimatedNumber.Slottable.serializer()
        }

        val value = requireNotNull(element.jsonObject["k"]) {
            "Illegal animated number encoding: $element"
        }

        val animated = element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 1 ||
                value is JsonObject || value is JsonArray && value.firstOrNull() is JsonObject

        return if (animated) {
            AnimatedNumber.Animated.serializer()
        } else {
            AnimatedNumber.Default.serializer()
        }
    }
}

private object AnimatedNumberAsPrimitiveSerializer :
    JsonTransformingSerializer<AnimatedNumber.Default>(AnimatedNumber.Default.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonObject(mapOf("k" to element))
    }
}