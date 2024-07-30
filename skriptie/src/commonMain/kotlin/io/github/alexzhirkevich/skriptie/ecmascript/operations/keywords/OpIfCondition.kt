package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant

internal class OpIfCondition<C : ScriptContext>(
    val condition : Expression<C> = OpConstant(true),
    val onTrue : Expression<C>? = null,
    val onFalse : Expression<C>? = null
) : Expression<C> {

    override fun invoke(
        context: C
    ): Undefined {
        val expr = if (condition(context) as Boolean){
            onTrue
        } else {
            onFalse
        }

        expr?.invoke(context)

        return Undefined
    }
}