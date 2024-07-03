package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpGetComp(
    private val name : Expression?
) : OpCompositionContext() {

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        return when (val n = name?.invoke(property, variables, state)) {
            null -> state.currentComposition
            state.composition.animation.name -> state.composition.expressionComposition
            else -> requireNotNull(state.composition.precomps[n]) {
                "Composition with name '$n' wasn't found"
            }
        }
    }
}