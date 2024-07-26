package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect

internal sealed class OpEffectContext : ExpressionContext<LayerEffect> {

    final override fun interpret(callable: String?, args: List<Expression>?): Expression? {

        return when (callable) {
            null -> {
                checkArgs(args, 1, "()")
                OpGetEffectParam(this, args.argAt(0))
            }
            "active" -> withContext { _, _, _ -> enabled }
            "param" -> {
                checkArgs(args, 1, callable)
                OpGetEffectParam(this, args.argAt(0))
            }

            else -> null
        }
    }
}