package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JsWrapper

internal fun  OpEquals(
    a : Expression,
    b : Expression,
    isTyped : Boolean
) = Expression {
    OpEqualsImpl(a(it), b(it), isTyped)
}

internal tailrec fun OpEqualsImpl(a : Any?, b : Any?, typed : Boolean) : Boolean {
    return when {
        a == null || b == null -> a == b
        a is Number && b is Number -> a.toDouble() == b.toDouble()
        typed || a::class == b::class -> {
            if (a is JsWrapper<*> && b is JsWrapper<*>) {
                OpEqualsImpl(a.value, b.value, typed)
            } else {
                a == b
            }
        }
        a is String && b is Number -> a.toDoubleOrNull() == b.toDouble()
        b is String && a is Number -> b.toDoubleOrNull() == a.toDouble()
        else -> a.toString() == b.toString()
    }
}