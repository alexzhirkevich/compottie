package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect

internal sealed class OpEffectContext : ExpressionContext<LayerEffect> {

    final override fun interpret(op: String, args: List<Expression>): Expression? {

        return when(op){
            "active" -> withContext { _, _, _ -> enabled }

            else -> null
        }
    }
}