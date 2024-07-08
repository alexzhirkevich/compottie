package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.get

internal class OpRgbToHsl(
    private val rgb : Expression
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val color = rgb(property, context, state)

        val r = (color[0] as Number).toFloat()
        val g = (color[1] as Number).toFloat()
        val b = (color[2] as Number).toFloat()
        val a = (color[3] as Number).toFloat()

        val max = maxOf(r, g, b);
        val min = minOf(r, g, b);
        var h: Float
        val s: Float
        val l = (max + min) / 2;

        if (max == min) {
            h = 0f; // achromatic
            s = 0f; // achromatic
        } else {
            val d = max - min;
            s = if (l > 0.5) d / (2 - max - min) else d / (max + min)
            h = when (max) {
                r -> (g - b) / d + (if (g < b) 6 else 0)
                g -> (b - r) / d + 2
                b -> (r - g) / d + 4
                else -> error("Should never happend")
            }
            h /= 6
        }

        return mutableListOf(h, s, l, a)
    }
}