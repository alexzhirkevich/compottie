package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpGetLayer(
    private val name : Operation? = null
) : OpLayerContext() {

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return if (name == null) {
            state.layer
        } else {
            val n = name.invoke(value, variables, state) as String
            checkNotNull(state.composition.animation.layersByName[n]) {
                "Layer with name '$name' not found"
            }
        }
    }
}