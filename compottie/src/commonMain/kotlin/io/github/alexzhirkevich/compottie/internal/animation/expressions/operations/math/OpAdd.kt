package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.min


internal fun OpAdd(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) + b(property, context, state)
}

internal fun OpAdd(a : Any, b : Any) : Any = a.plus(b)

internal operator fun Any.plus(other : Any) : Any {
    val a = this
    return when {
        a is Number && other is Number -> a.toFloat() + other.toFloat()
        a is List<*> && other is List<*> -> {
            a as List<Number>
            other as List<Number>

            return List(min(a.size, other.size)) {
                a[it].toFloat() + other[it].toFloat()
            }
        }

        a is List<*> && other is Number -> (a as List<Number>).first().toFloat() + other.toFloat()
        a is Number && other is List<*> -> a.toFloat() + (other as List<Number>).first().toFloat()
        a is CharSequence -> a.toString() + other.toString()
        other is CharSequence -> a.toString() + other

        else -> error("Cant calculate the sum of $a and $other")
    }
}