package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.min

internal fun OpSub(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) - b(property, context, state)
}

internal fun OpSub(a : Any, b : Any) : Any = a.minus(b)

internal operator fun Any.minus(other : Any) : Any {
    val a = this
    return when {
        a is Number && other is Number -> a.toFloat() - other.toFloat()
        a is List<*> && other is List<*> -> {
            a as List<Number>
            other as List<Number>
            List(min(a.size, other.size)) {
                a[it].toFloat() - other[it].toFloat()
            }
        }
        a is CharSequence || other is CharSequence -> {
            a.toString().toFloat() - other.toString().toFloat()
        }
        else -> error("Cant subtract $other from $a")
    }
}