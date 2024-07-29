package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal fun OpMul(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) * b(property, context, state)
}

internal fun OpMul(a : Any, b : Any) : Any = a.times(b)

internal operator fun Any.times(other : Any) : Any  {
    val a = validateJsNumber()
    val b = other.validateJsNumber()
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a*b
        a is Double && b is Double -> a*b
        a is Long && b is Long -> if (b == 0) Float.POSITIVE_INFINITY else a/b
        a is Number && b is Number -> a.toDouble() * b.toDouble()
        a is List<*> && b is Number -> {
            a as List<Number>
            val bf = b.toDouble()
            a.fastMap { it.toDouble() * bf }
        }
        a is Number && b is List<*> -> {
            b as List<Number>
            val af = a.toDouble()
            b.fastMap { it.toDouble() * af }
        }
        a is CharSequence || b is CharSequence -> {
            a.toString().toDouble() * b.toString().toDouble()
        }
        else -> error("Cant multiply $a by $b")
    }
}