package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpUnaryPlus(a : Expression) = Expression { property, context, state ->
    when (val v = a(property, context, state)) {
        is Number -> v
        is List<*> -> v
        else -> error("Cant apply unary plus to $v")
    }
}
