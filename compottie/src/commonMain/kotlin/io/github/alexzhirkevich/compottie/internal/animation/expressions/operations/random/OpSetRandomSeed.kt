package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal fun OpSetRandomSeed(
    seed : Expression,
    timeless : Expression? = null
) = Expression { property, context, state ->
    context.randomSource.setSeed(
        seed = (seed(property, context, state) as Number).toInt(),
        timeless = (timeless?.invoke(property, context, state) as? Boolean) ?: false
    )

    Undefined
}