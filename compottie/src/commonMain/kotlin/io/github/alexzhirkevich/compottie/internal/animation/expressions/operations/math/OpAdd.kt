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
        a is Number && b is Undefined || a is Undefined && b is Number -> Double.NaN
        a is Long && b is Long -> a+b
        a is Number && b is Number -> a.toDouble() + b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>

            List(min(a.size, b.size)) {
                a[it].toDouble() + b[it].toDouble()
            }
        }

        a is List<*> && b is Number -> {
            if (a is MutableList<*>){
                a as MutableList<Number>
                a[0] = a[0].toDouble() + b.toDouble()
                a
            } else {
                ((a as List<Number>).first().toDouble() + b.toDouble()) + a.drop(1)
            }
        }
        a is Number && b is List<*> -> {
            if (b is MutableList<*>){
                b as MutableList<Number>
                b[0] = b[0].toDouble() + a.toDouble()
                b
            } else {
                (a.toDouble() + (b as List<Number>).first().toDouble()) + b.drop(1)
            }
        }
        a is CharSequence -> a.toString() + b.toString()

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