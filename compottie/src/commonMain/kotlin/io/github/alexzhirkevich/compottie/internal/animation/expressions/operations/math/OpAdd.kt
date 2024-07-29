package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import kotlin.math.min


internal fun OpAdd(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) + b(property, context, state)
}

internal fun OpAdd(a : Any, b : Any) : Any = a.plus(b)

internal operator fun Any.plus(other : Any) : Any {
    val a = validateJsNumber()
    val b = other.validateJsNumber()
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a+b
        a is Double && b is Double -> a+b
        a is Number && b is Number -> a.toDouble() + b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>

            return List(min(a.size, b.size)) {
                a[it].toDouble() + b[it].toDouble()
            }
        }

        a is List<*> && b is Number -> (a as List<Number>).first().toDouble() + b.toDouble()
        a is Number && b is List<*> -> a.toDouble() + (b as List<Number>).first().toDouble()
        a is CharSequence -> a.toString() + b.toString()
        b is CharSequence -> a.toString() + b

        else -> error("Cant calculate the sum of $a and $b")
    }
}

internal fun Any.validateJsNumber() = when(this) {
    is Byte -> toLong()
    is UByte -> toLong()
    is Short -> toLong()
    is UShort -> toLong()
    is Int -> toLong()
    is UInt -> toLong()
    is ULong -> toLong()
    is Float -> toDouble()
    else -> this
}