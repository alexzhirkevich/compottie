package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.get
import io.github.alexzhirkevich.compottie.internal.utils.hslToBlue
import io.github.alexzhirkevich.compottie.internal.utils.hslToGreen
import io.github.alexzhirkevich.compottie.internal.utils.hslToRed

internal class OpHslToRgb(
    private val hsl : Expression
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val hsl = hsl(property, context, state)

        val h = (hsl[0] as Number).toFloat()
        val s = (hsl[1] as Number).toFloat()
        val l = (hsl[2] as Number).toFloat()
        val a = (hsl[3] as Number).toFloat()


        return mutableListOf(
            hslToRed(h, s, l),
            hslToGreen(h, s, l),
            hslToBlue(h, s, l),
            a
        )
    }
}