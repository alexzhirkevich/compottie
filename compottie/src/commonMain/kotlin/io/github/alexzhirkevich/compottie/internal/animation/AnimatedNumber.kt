package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

@Serializable(with = AnimatedNumberSerializer::class)
internal sealed interface AnimatedNumber : KeyframeAnimation<Float>, Indexable {

    @Serializable
    class Default(
        @SerialName("k")
        val value: Float,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedNumber {

        override fun interpolated(state: AnimationState): Float = value
    }

    @Serializable
    class Animated(
        @SerialName("k")
        val value: List<ValueKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedNumber, KeyframeAnimation<Float> by BaseKeyframeAnimation(
        expression = expression,
        keyframes = value,
        emptyValue = 1f,
        map = { s, e, p ->
            lerp(s[0], e[0], easingX.transform(p))
        }
    )
}

internal fun AnimatedNumber.interpolatedNorm(state: AnimationState) = interpolated(state) / 100f
internal class AnimatedNumberSerializer : JsonContentPolymorphicSerializer<AnimatedNumber>(AnimatedNumber::class){
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedNumber> {

        val value = requireNotNull(element.jsonObject["k"]){
            "Unrecognized animation value: $element"
        }

        return if (value is JsonPrimitive){
            AnimatedNumber.Default.serializer()
        } else {
            AnimatedNumber.Animated.serializer()
        }
    }
}
