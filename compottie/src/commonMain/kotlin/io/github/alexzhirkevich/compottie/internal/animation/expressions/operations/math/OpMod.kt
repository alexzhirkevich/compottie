package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpMod(
    a : Expression,
    b : Expression,
) = Expression { property, context, state ->
    OpMod(
        a(property, context, state),
        b(property, context, state)
    )
}

internal fun OpMod(a : Any, b : Any) : Any {
    val a = a.validateJsNumber()
    val b = b.validateJsNumber()
    return when {
        a is Long && b is Long -> a % b
        a is Number && b is Number -> a.toDouble() % b.toDouble()
        else -> error("Can't get mod of $a and $b")
    }
}
