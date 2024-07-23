package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.hypot

internal fun OpLength(
    a : Expression,
    b : Expression? = null,
) = Expression { property, context, state ->
    val a = a(property, context, state)
    val b = b?.invoke(property, context, state)

    when {
        a is List<*> && b == null -> hypot((a[0] as Number).toFloat(), (a[1] as Number).toFloat())
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>
            hypot(b[0].toFloat() - a[0].toFloat(), b[1].toFloat() - a[1].toFloat())
        }

        else -> error("Cant calculate the normalize() of $a and $b")
    }
}