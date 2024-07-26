package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal fun OpWhileLoop(
    condition : Expression,
    body : Expression
) = Expression { property, context, state ->

    while (!condition.invoke(property, context, state).isFalse()){
        body.invoke(property, context, state)
    }
}

internal fun Any.isFalse() : Boolean {
    return this == false
            || this is Number && toFloat() == 0f
            || this is CharSequence && isEmpty()
            || this is Undefined
}

