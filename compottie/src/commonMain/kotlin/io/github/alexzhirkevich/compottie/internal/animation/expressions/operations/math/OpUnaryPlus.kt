package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpUnaryPlus(
    private val v : Expression,
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        return when (val v = v(property, context, state)) {
            is Number -> v
            is List<*> -> v
            else -> error("Cant apply unary plus to $v")
        }
    }
}