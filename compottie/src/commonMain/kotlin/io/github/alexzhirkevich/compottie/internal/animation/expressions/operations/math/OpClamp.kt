package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpClamp(
    v : Expression,
    from : Expression,
    to : Expression,
) = Expression { property, context, state ->
    val v = v(property, context, state).validateJsNumber()
    val from = from(property, context, state).validateJsNumber()
    val to = to(property, context, state).validateJsNumber()

    require(v is Number && from is Number && to is Number) {
        "Cant clamp ($v, $from, $to) : not a number"
    }

    if (v is Long && from is Long && to is Long) {
        v.coerceIn(from, to)
    } else {
        v.toDouble().coerceIn(from.toDouble(), to.toDouble(),)
    }
}