package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

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
            mColorStops[i] = androidx.compose.ui.util.lerp(a.colorStops[i], b.colorStops[i], progress)
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