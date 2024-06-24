package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.jvm.JvmInline

internal class ColorsWithStops(
    size: Int
) {
    val colorStops: List<Float> get() = mColorStops
    val colors: List<Color> get() = mColors

    private val mColorStops: MutableList<Float> = ArrayList(size)
    private val mColors: MutableList<Color> = ArrayList(size)

    fun fill(colors: FloatArray, numberOfColors: Int) {
        resizeTo(numberOfColors)

        repeat(numberOfColors) {
            mColorStops[it] = colors[it * 4]

            val alpha = if (colors.size == numberOfColors * 6) {
                colors[colors.lastIndex - numberOfColors * 2 + (it + 1) * 2]
            } else 1f


            mColors[it] =
                Color(
                    red = colors[it * 4 + 1],
                    green = colors[it * 4 + 2],
                    blue = colors[it * 4 + 3],
                    alpha = alpha
                )
        }
    }

    fun fill(colors: List<Float>, numberOfColors: Int) {
        resizeTo(numberOfColors)

        repeat(numberOfColors) {
            mColorStops[it] = colors[it * 4]

            val alpha = if (colors.size == numberOfColors * 6) {
                colors[colors.lastIndex - numberOfColors * 2 + (it + 1) * 2]
            } else 1f


            mColors[it] =
                Color(
                    red = colors[it * 4 + 1],
                    green = colors[it * 4 + 2],
                    blue = colors[it * 4 + 3],
                    alpha = alpha
                )
        }
    }


    fun interpolateBetween(a: ColorsWithStops, b: ColorsWithStops, progress: Float) {
        val n = minOf(a.colors.size, b.colors.size)

        resizeTo(n)

        repeat(n) { i ->
            mColors[i] = lerp(a.colors[i], b.colors[i], progress)
            mColorStops[i] =
                androidx.compose.ui.util.lerp(a.colorStops[i], b.colorStops[i], progress)
        }
    }

    private fun resizeTo(size: Int) {
        while (colorStops.size < size) {
            mColorStops.add(0f)
            mColors.add(Color.Transparent)
        }
        while (colorStops.size > size) {
            mColorStops.removeLast()
            mColors.removeLast()
        }
    }
}


@Serializable
internal class GradientColors(

    @SerialName("k")
    val colors: AnimatedGradient,

    @SerialName("p")
    val numberOfColors: Int = 0
) {
    fun copy()  = GradientColors(
        colors = colors.copy(),
        numberOfColors = numberOfColors
    )

}

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
internal abstract class AnimatedGradient : KeyframeAnimation<ColorsWithStops> {

    @Transient
    var numberOfColors: Int = 0

    abstract fun copy() : AnimatedGradient

    @SerialName("0")
    @Serializable
    class Default(
        @SerialName("k")
        val colorsVector: FloatArray,
    ) : AnimatedGradient() {

        private val tempColors by lazy {
            ColorsWithStops(numberOfColors).apply {
                fill(colorsVector, numberOfColors)
            }
        }

        override fun interpolated(state: AnimationState): ColorsWithStops {
            return tempColors
        }

        override fun copy(): AnimatedGradient {
            return Default(colorsVector.copyOf())
        }
    }

    @SerialName("1")
    @Serializable
    class Animated(
        @SerialName("k")
        val keyframes: List<VectorKeyframe>,
    ) : AnimatedGradient(), KeyframeAnimation<ColorsWithStops> {

        private val tempColors by lazy {
            ColorsWithStops(numberOfColors)
        }

        private val tempColorsA by lazy {
            ColorsWithStops(numberOfColors)
        }

        private val tempColorsB by lazy {
            ColorsWithStops(numberOfColors)
        }

        @Transient
        private val delegate = BaseKeyframeAnimation(
            expression = null,
            keyframes = keyframes,
            emptyValue = tempColors
        ) { s, e, p ->
            val progress = easingX.transform(p)

            tempColorsA.fill(s, numberOfColors)
            tempColorsB.fill(e, numberOfColors)

            tempColors.apply {
                interpolateBetween(tempColorsA, tempColorsB, progress)
            }
        }

        override fun copy(): AnimatedGradient {
            return Animated(keyframes)
        }

        override fun interpolated(state: AnimationState): ColorsWithStops {
            return delegate.interpolated(state)
        }
    }
}
