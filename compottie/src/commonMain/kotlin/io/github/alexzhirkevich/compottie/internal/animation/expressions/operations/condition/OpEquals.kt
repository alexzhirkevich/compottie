package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.condition

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpEquals(
    a : Expression,
    b : Expression,
    isTyped : Boolean
) = Expression { property, context, state ->
    val a = a(property, context, state)
    val b = b(property, context, state)

    when {
        isTyped || a::class == b::class -> a == b
        a is Number && b is Number -> a.toDouble() == b.toDouble()
        a is String && b is Number -> a.toDoubleOrNull() == b.toDouble()
        b is String && a is Number -> b.toDoubleOrNull() == a.toDouble()
        else -> a.toString() == b.toString()
    }
}