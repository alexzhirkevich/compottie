package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.lerp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedValue : KeyframeAnimation<Float>, Indexable {

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value: Float,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedValue {

        override fun interpolated(frame: Float): Float = value
    }

    @Serializable
    @SerialName("1")
    class Animated(
        @SerialName("k")
        val value: List<ValueKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedValue, KeyframeAnimation<Float> by BaseKeyframeAnimation(
        keyframes = value,
        emptyValue = 1f,
        map = { s, e, p, _ ->
            lerp(s[0], e[0], easingX.transform(p))
        }
    )
}

