package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpNoise(
    time: Expression
) = Expression { property, context, state ->
    when (val time = time.invoke(property, context, state)){
        is Number -> context.randomSource.noise(time.toFloat())
        is Vec2 -> Vec2(
            context.randomSource.noise(time.x),
            context.randomSource.noise(time.y)
        )
        else -> error("noise() takes single float or vector argument but $time got")
    }
}