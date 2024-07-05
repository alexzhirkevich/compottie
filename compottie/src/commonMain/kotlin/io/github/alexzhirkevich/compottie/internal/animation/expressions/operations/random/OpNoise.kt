package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpNoise(
    private val time : Expression
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        return when (val time = time.invoke(property, context, state)){
            is Number -> context.randomSource.noise(time.toFloat())
            is Vec2 -> Vec2(
                context.randomSource.noise(time.x),
                context.randomSource.noise(time.y)
            )
            else -> error("noise() takes single float or vector argument but $time got")
        }
    }
}