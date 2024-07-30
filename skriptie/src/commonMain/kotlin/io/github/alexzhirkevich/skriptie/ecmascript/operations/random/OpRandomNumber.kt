package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpRandomNumber(
    minValOrArray1 : Expression? = null,
    minValOrArray2 : Expression? = null,
    isGauss : Boolean = false
) = Expression { property, context, state ->
    with(context.random) {
        when {
            minValOrArray1 == null && minValOrArray2 == null ->
                if (isGauss) gaussRandom() else random()

            minValOrArray2 == null && minValOrArray1 != null ->
                if (isGauss) {
                    gaussRandom(minValOrArray1.invoke(context))
                } else {
                    random(minValOrArray1.invoke(context))
                }

            minValOrArray2 != null && minValOrArray1 != null ->
                if (isGauss) {
                    gaussRandom(
                        minValOrArray1.invoke(context),
                        minValOrArray2.invoke(context),
                    )
                } else {
                    random(
                        minValOrArray1.invoke(context),
                        minValOrArray2.invoke(context),
                    )
                }

            else -> error("Invalid parameters for random()")
        }
    }
}