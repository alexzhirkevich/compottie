package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpDot(
    a : Expression,
    b : Expression,
) = Expression { property, context, state ->
    val a = a(property, context, state)
    val b = b(property, context, state)

    when {
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>
            a[0].toFloat() * b[0].toFloat() + a[1].toFloat() * b[1].toFloat()
        }

        else -> error("Cant calculate the dot() of $a and $b")
    }
}