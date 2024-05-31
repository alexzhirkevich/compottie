package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.jvm.JvmInline

internal class ColorsWithStops(
    val colorStops: List<Float>,
    val colors : List<Color>
)

@Serializable
internal class GradientColors(

    @SerialName("k")
    val colors: AnimatedGradient,

    @SerialName("p")
    val numberOfColors: Int = 0
)

@Serializable
@JvmInline
internal value class GradientType(val type : Byte) {
    companion object {
        val Linear = GradientType(1)
        val Radial = GradientType(2)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedGradient : KeyframeAnimation<ColorsWithStops> {

    var numberOfColors: Int

    @SerialName("0")
    @Serializable
    class Default(
        @SerialName("k")
        val colorsVector: FloatArray,
    ) : AnimatedGradient {

        @Transient
        override var numberOfColors: Int = 0

        private val colors by lazy {
            colorsVector.asGradient(numberOfColors)
        }

        override fun interpolated(frame: Float): ColorsWithStops {
            return colors
        }
    }
}

private fun FloatArray.asGradient(numberOfColors: Int) : ColorsWithStops = ColorsWithStops(
    colorStops = (0 until numberOfColors).map {
        this[it * 4]
    },
    colors = (0 until numberOfColors).map {

        val alpha = if (size == numberOfColors * 6) {
            this[lastIndex - numberOfColors * 2 + (it + 1) * 2]
        } else 1f

        Color(
            red = this[it * 4 + 1],
            green = this[it * 4 + 2],
            blue = this[it * 4 + 3],
            alpha = alpha
        )
    }
)