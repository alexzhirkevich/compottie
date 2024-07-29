package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import kotlin.math.min

internal fun OpSub(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) - b(property, context, state)
}

internal fun OpSub(a : Any, b : Any) : Any = a.minus(b)

internal operator fun Any.minus(other : Any) : Any {
    val a = validateJsNumber()
    val b = other.validateJsNumber()
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a-b
        a is Double && b is Double -> a-b
        a is Number && b is Number -> a.toDouble() - b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>
            List(min(a.size, b.size)) {
                a[it].toDouble() - b[it].toDouble()
            }
        }
        a is CharSequence || b is CharSequence -> {
            a.toString().toDouble() - b.toString().toDouble()
        }
        else -> error("Cant subtract $b from $a")
    }
}