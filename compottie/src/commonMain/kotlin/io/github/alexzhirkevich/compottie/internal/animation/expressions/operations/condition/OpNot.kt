package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.condition

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpNot(
    condition : Expression
) = Expression { property, context, state ->
    !(condition(property, context, state) as Boolean)
}