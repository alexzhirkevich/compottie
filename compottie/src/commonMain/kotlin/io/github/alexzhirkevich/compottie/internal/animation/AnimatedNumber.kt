package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

@Serializable(with = AnimatedNumberSerializer::class)
internal sealed class AnimatedNumber : PropertyAnimation<Float> {

    protected var dynamic: PropertyProvider<Float>? = null
        private set

    abstract val expression : String?

    @Transient
    private val expressionEvaluator by lazy {
        expression?.let(::ExpressionEvaluator)
    }

    fun dynamic(provider: PropertyProvider<Float>?) {
        dynamic = provider
    }

    abstract fun copy() : AnimatedNumber

    final override fun interpolated(state: AnimationState): Float {
        val v = expressionEvaluator
            ?.evaluate(this, state) as? Float
            ?: rawInterpolated(state)
        return dynamic.derive(v, state)
    }

    @Serializable
    class Default(
        @SerialName("k")
        val value: Float,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null
    ) : AnimatedNumber() {

        override fun copy(): AnimatedNumber {
            return Default(
                value = value,
                expression = expression,
                index = index
            )
        }

        override fun rawInterpolated(state: AnimationState): Float = value
    }

    @Serializable
    class Animated(
        @SerialName("k")
        override val keyframes: List<ValueKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null
    ) : AnimatedNumber(), KeyframeAnimation<Float, ValueKeyframe> {

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

        override fun rawInterpolated(state: AnimationState): Float {
            return delegate.interpolated(state)
        }
    }
}

internal fun AnimatedNumber.dynamicNorm(provider: PropertyProvider<Float>?) {
    if (provider != null)
        dynamic { provider(it) * 100f }
    else dynamic(null)
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