package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext


internal fun <C : ScriptContext> OpIfCondition(
    condition : Expression<C> = OpConstant(true),
    onTrue : Expression<C>? = null,
    onFalse : Expression<C>? = null
) = Expression<C> {
    val expr = if (condition(it) as Boolean){
        onTrue
    } else {
        onFalse
    }

    expr?.invoke(it)

    Unit
}