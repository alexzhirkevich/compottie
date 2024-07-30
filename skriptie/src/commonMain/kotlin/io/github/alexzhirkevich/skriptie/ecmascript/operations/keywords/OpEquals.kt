package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext

internal fun <C : ScriptContext> OpEquals(
    a : Expression<C>,
    b : Expression<C>,
    isTyped : Boolean
) = Expression<C> {
    OpEqualsImpl(a(it), b(it), isTyped)
}

internal fun OpEqualsImpl(a : Any, b : Any, typed : Boolean) : Boolean {

    return when {
        a is Number && b is Number -> a.toDouble() == b.toDouble()
        typed || a::class == b::class -> a == b
        a is String && b is Number -> a.toDoubleOrNull() == b.toDouble()
        b is String && a is Number -> b.toDoubleOrNull() == a.toDouble()
        else -> a.toString() == b.toString()
    }
}