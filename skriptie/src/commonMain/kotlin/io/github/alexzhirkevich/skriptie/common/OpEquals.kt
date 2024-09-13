package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JsWrapper

internal fun  OpEquals(
    a : Expression,
    b : Expression,
    isTyped : Boolean
) = Expression {
    OpEqualsImpl(a(it), b(it), isTyped, it)
}

internal fun OpEqualsImpl(a : Any?, b : Any?, typed : Boolean, runtime: ScriptRuntime) : Boolean {

    if (!typed) {
        if (a is JsWrapper<*>) {
            return OpEqualsImpl(a.value, b, typed, runtime)
        }

        if (b is JsWrapper<*>) {
            return OpEqualsImpl(a, b.value, typed, runtime)
        }
    }

    return when {
        a == null || b == null -> a == b
        typed -> a::class == b::class && OpEqualsImpl(a, b, false, runtime)
        a::class == b::class -> a == b
        b is Number -> runtime.toNumber(a).toDouble() == b.toDouble()
        a is Number -> runtime.toNumber(b).toDouble() == a.toDouble()
        else -> a.toString() == b.toString()
    }
}