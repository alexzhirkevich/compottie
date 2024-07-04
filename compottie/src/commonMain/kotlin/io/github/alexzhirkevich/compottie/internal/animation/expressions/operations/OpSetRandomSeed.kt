package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpSetRandomSeed(
    private val seed : Expression,
    private val timeless : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Undefined {
        context.randomSource.setSeed(
            seed = (seed(property, context, state) as Number).toInt(),
            timeless = (timeless?.invoke(property, context, state) as? Boolean) ?: false
        )

        return Undefined
    }
}