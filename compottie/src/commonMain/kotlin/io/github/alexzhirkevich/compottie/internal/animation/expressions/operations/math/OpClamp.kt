package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpClamp(
    v : Expression,
    from : Expression,
    to : Expression,
) = Expression { property, context, state ->
    val v = v(property, context, state)
    val from = from(property, context, state)
    val to = to(property, context, state)

    require(v is Number && from is Number && to is Number) {
        "Cant clamp ($v, $from, $to) : not a number"
    }

    v.toFloat().coerceIn(from.toFloat(), to.toFloat(),)
}