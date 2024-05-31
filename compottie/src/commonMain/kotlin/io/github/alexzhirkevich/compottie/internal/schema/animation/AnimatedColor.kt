
package io.github.alexzhirkevich.compottie.internal.schema.animation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedColor : Animated<Color>, Indexable {

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedColor {

        @Transient
        private val color: Color = value.asColor()

        override fun interpolated(frame: Int) = color
    }

    @Serializable
    @SerialName("1")
    class Keyframed(

        @SerialName("k")
        val value: List<VectorKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("ti")
        val inTangent: FloatArray? = null,

        @SerialName("to")
        val outTangent: FloatArray? = null,
    ) : AnimatedColor, Animated<Color> by KeyframeAnimation(
        keyframes = value,
        emptyValue = Color.Transparent,
        map = { s, e, p ->
            lerp(s.asColor(), e.asColor(), p)
        }
    )
}

private fun FloatArray.asColor() = Color(
    red = get(0),
    green = get(1),
    blue = get(2),
    alpha = getOrNull(3) ?: 1f
)



