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
    return when {
        a is Int && b is Int -> a % b
        a is Long && b is Long -> a % b
        a is Int && b is Long || a is Long && b is Int -> (a as Number).toLong() % (b as Number).toLong()
        a is Number && b is Number -> a.toFloat() % b.toFloat()
        else -> error("Can't get mod of $a and $b")
    }
}
