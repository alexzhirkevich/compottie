package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.utils.degreeToRadians
import io.github.alexzhirkevich.compottie.internal.utils.radiansToDegree

internal fun OpDegreesToRadians(
    degrees : Expression
) = Expression { property, context, state ->
    degreeToRadians((degrees(property, context,state) as Number).toFloat())
}

internal fun OpRadiansToDegree(
    rad : Expression
) = Expression { property, context, state ->
    radiansToDegree((rad(property, context,state) as Number).toFloat())
}