package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect

internal sealed class OpEffectContext : ExpressionContext<LayerEffect> {

    final override fun parse(op: String, args: List<Expression>): Expression {

        return when(op){


            else -> unresolvedProperty(op, "Effect")
        }
    }
}