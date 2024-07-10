package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpMod(
    private val a : Expression,
    private val b : Expression,
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val a = a(property, context, state)
        val b = b(property, context, state)

        require(a is Number && b is Number) {
            "Can't get mod of $a and $b"
        }
        return a.toFloat().mod(b.toFloat())
    }
}