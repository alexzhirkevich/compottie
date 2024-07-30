package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal fun OpSetRandomSeed(
    seed : Expression,
    timeless : Expression? = null
) = Expression { property, context, state ->
    context.random.setSeed(
        seed = (seed(context) as Number).toInt(),
        timeless = (timeless?.invoke(context) as? Boolean) ?: false
    )

    Undefined
}