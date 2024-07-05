package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition

internal class OpGetComp(
    private val name : Expression?
) : OpCompositionContext() {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): ExpressionComposition {

        return when (val n = name?.invoke(property, context, state)) {
            null -> state.currentComposition
            state.composition.animation.name -> state.composition.expressionComposition
            else -> requireNotNull(state.composition.precomps[n]) {
                "Composition with name '$n' wasn't found"
            }
        }
    }
}