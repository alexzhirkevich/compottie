package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpMod(
    a : Expression,
    b : Expression,
) = Expression { property, context, state ->
    val a = a(property, context, state)
    val b = b(property, context, state)

    require(a is Number && b is Number) {
        "Can't get mod of $a and $b"
    }
    a.toFloat().mod(b.toFloat())
}