package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.min


@Serializable(with = AnimatedVectorNSerializer::class)
internal sealed class AnimatedVectorN : DynamicProperty<List<Float>>() {


    override fun mapEvaluated(e: Any): List<Float> {
        return when (e) {
            is List<*> -> e as List<Float>
            else -> error("Failed to cast $e to Vec2")
        }
    }

    abstract fun copy() : AnimatedVectorN


    @Serializable
    class Default(
        @SerialName("k")
        val value: List<Float>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null
    ) : AnimatedVectorN() {

        init {
            prepare()
        }

        @Transient
        private val vec = Vec2(value[0], value[1])

        override fun raw(state: AnimationState): List<Float> = value

        override fun copy(): AnimatedVectorN {
            return Default(
                value = value,
                expression = expression,
                index = index
            )
        }
    }

    @Serializable
    class Animated(
        @SerialName("k")
        override val keyframes: List<VectorKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null
    ) : AnimatedVectorN(), AnimatedKeyframeProperty<List<Float>, VectorKeyframe> {

        init {
            prepare()
        }

        @Transient
        private val delegate = BaseKeyframeAnimation(
            index = index,
            keyframes = keyframes,
            emptyValue = emptyList(),
            map = { s, e, p ->
                val p = easingX.transform(p)

                List(min(s.size, e.size)) {
                    lerp(s[it], e[it], p)
                }
            }
        )

        override fun raw(state: AnimationState): List<Float> {
            return delegate.raw(state)
        }

        override fun copy(): AnimatedVectorN {
            return Animated(
                keyframes = keyframes,
                expression = expression,
                index = index
            )
        }
    }
}

internal class AnimatedVectorNSerializer : JsonContentPolymorphicSerializer<AnimatedVectorN>(AnimatedVectorN::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedVectorN> {

        val k = element.jsonObject["k"]

        return when {

            element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 1 ||
                    k is JsonArray && k[0] is JsonObject ->
                AnimatedVectorN.Animated.serializer()

            else -> AnimatedVectorN.Default.serializer()
        }
    }
}
