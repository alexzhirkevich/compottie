package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedGradient : Animated<ColorsWithStops> {

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
            ColorsWithStops(
                colorStops = (0 until numberOfColors).map {
                    colorsVector[it * 3]
                },
                colors = (0 until numberOfColors).map {

                    val alpha = if (colorsVector.size == numberOfColors * 6) {
                        colorsVector[colorsVector.lastIndex - numberOfColors * 2 + (it + 1) * 2]
                    } else 1f

                    Color(
                        red = colorsVector[it * 3 + 1],
                        green = colorsVector[it * 3 + 2],
                        blue = colorsVector[it * 3 + 3],
                        alpha = alpha
                    ).also { println(it) }

                }
            )
        }

        override fun interpolated(frame: Int): ColorsWithStops {
            return colors
        }
    }
}