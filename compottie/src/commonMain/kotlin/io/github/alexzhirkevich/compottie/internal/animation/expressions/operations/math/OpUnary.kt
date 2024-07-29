package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpUnaryMinus(a : Expression) = Expression { property, context, state ->
    when (val v = a(property, context, state).validateJsNumber()) {
        is Long -> -v
        is Number -> -v.toDouble()
        is List<*> -> {
            v as List<Number>
            v.fastMap { -it.toDouble() }
        }

        else -> error("Cant apply unary minus to $v")
    }
}

internal fun OpUnaryPlus(a : Expression) = Expression { property, context, state ->
    when (val v = a(property, context, state)) {
        is Number -> v
        is List<*> -> v
        else -> error("Cant apply unary plus to $v")
    }
}
