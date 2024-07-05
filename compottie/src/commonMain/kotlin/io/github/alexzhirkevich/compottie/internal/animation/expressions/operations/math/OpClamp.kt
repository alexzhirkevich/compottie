package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpClamp(
    private val v : Expression,
    private val from : Expression,
    private val to : Expression,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Float {
        val v = v(property, context, state)
        val from = from(property, context, state)
        val to = to(property, context, state)

        require(v is Number && from is Number && to is Number) {
            "Cant clamp ($v, $from, $to) : not a number"
        }

        return v.toFloat().coerceIn(from.toFloat(), to.toFloat(),)
    }
}