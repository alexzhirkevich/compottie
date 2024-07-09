package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.invoke
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

@Serializable(with = AnimatedNumberSerializer::class)
internal sealed class AnimatedNumber : DynamicProperty<Float>() {


    override fun mapEvaluated(e: Any): Float {
        return when (e){
            is Number -> e.toFloat()
            is List<*> -> (e[0] as Number).toFloat()
            else -> error("Failed to cast $e to number")
        }
    }

    abstract fun copy() : AnimatedNumber

    @Serializable
    class Default(
        @SerialName("k")
        val value: Float,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null
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
        override val index: Int? = null
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

internal class AnimatedNumberSerializer : JsonContentPolymorphicSerializer<AnimatedNumber>(AnimatedNumber::class){
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedNumber> {

        if (element is JsonPrimitive){
            return AnimatedNumberAsPrimitiveSerializer
        }

        val value = requireNotNull(element.jsonObject["k"]){
            "Illegal animated number encoding: $element"
        }

        return if (value is JsonPrimitive){
            AnimatedNumber.Default.serializer()
        } else {
            AnimatedNumber.Animated.serializer()
        }
    }
}

private object AnimatedNumberAsPrimitiveSerializer :
    JsonTransformingSerializer<AnimatedNumber.Default>(AnimatedNumber.Default.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonObject(mapOf("k" to element))
    }
}