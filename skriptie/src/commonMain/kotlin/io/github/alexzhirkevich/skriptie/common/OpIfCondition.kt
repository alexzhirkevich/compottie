package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke


internal fun OpIfCondition(
    condition : Expression,
    onTrue : Expression? = null,
    onFalse : Expression? = null,
    expressible : Boolean = false
) = Expression {
    val expr = if (it.isFalse(condition(it))) onFalse else onTrue

    val res = expr?.invoke(it)

    if (expressible) {
        res
    } else {
        Unit
    }
}