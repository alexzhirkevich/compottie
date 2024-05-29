
package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.time.Duration.Companion.milliseconds

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
        private val color: Color = Color(
            red = value[0],
            green = value[1],
            blue = value[2],
            alpha = value.getOrNull(3) ?: 1f
        )

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
    ) : AnimatedColor {

        @Transient
        private val animation = value.toColorAnimation()

        override fun interpolated(time: Int): Color {
            return animation.getValueFromNanos(
                playTimeNanos = time.milliseconds.inWholeNanoseconds,
            ).let {
                Color(it.v1,it.v2,it.v3,it.v4)
            }
        }
    }
}



