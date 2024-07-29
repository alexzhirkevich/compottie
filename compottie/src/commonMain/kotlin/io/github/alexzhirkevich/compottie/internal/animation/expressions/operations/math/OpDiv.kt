package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal fun OpDiv(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) / b(property, context, state)
}

internal fun OpDiv(a : Any, b : Any) : Any = a.div(b)

internal operator fun Any.div(other : Any) : Any {
    val a = validateJsNumber()
    val b = other.validateJsNumber()

    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> when {
            b == 0 -> Double.POSITIVE_INFINITY
            a % b == 0L -> a / b
            else -> a.toDouble() / b
        }
        a is Number && b is Number -> a.toDouble() / b.toDouble()
        a is List<*> && b is Number -> {
            a as List<Number>
            val bf = b.toDouble()
            a.fastMap { it.toDouble() / bf }
        }
        a is CharSequence || b is CharSequence -> {
            a.toString().toDouble() / b.toString().toDouble()
        }
        else -> error("Cant divide $a by $b")
    }
}