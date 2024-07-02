package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpGetComp(
    private val name : Operation?
) : OpCompositionContext() {

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        val n = name?.invoke(value, variables, state)

        require(n == state.composition.animation.name){
            "Only self composition can be retrieved with comp()"
        }

        return state.composition
    }
}