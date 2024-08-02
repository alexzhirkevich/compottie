package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke


internal fun  OpIfCondition(
    condition : Expression = OpConstant(true),
    onTrue : Expression? = null,
    onFalse : Expression? = null
) = Expression {
    val expr = if (condition(it) as Boolean){
        onTrue
    } else {
        onFalse
    }

    expr?.invoke(it)

    Unit
}