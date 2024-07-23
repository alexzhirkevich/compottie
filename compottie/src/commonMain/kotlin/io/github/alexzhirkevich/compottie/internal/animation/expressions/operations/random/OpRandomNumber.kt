package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpRandomNumber(
    minValOrArray1 : Expression? = null,
    minValOrArray2 : Expression? = null,
    isGauss : Boolean = false
) = Expression { property, context, state ->
    with(context.randomSource) {
        when {
            minValOrArray1 == null && minValOrArray2 == null ->
                if (isGauss) gaussRandom() else random()

            minValOrArray2 == null && minValOrArray1 != null ->
                if (isGauss) {
                    gaussRandom(minValOrArray1.invoke(property, context, state))
                } else {
                    random(minValOrArray1.invoke(property, context, state))
                }

            minValOrArray2 != null && minValOrArray1 != null ->
                if (isGauss) {
                    gaussRandom(
                        minValOrArray1.invoke(property, context, state),
                        minValOrArray2.invoke(property, context, state),
                    )
                } else {
                    random(
                        minValOrArray1.invoke(property, context, state),
                        minValOrArray2.invoke(property, context, state),
                    )
                }

            else -> error("Invalid parameters for random()")
        }
    }
}