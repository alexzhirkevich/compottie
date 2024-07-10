package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpRandomNumber(
    private val minValOrArray1 : Expression? = null,
    private val minValOrArray2 : Expression? = null,
    private val isGauss : Boolean = false
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        return with(context.randomSource) {
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
}