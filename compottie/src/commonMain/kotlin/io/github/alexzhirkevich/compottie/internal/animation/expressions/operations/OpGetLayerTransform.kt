package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.layers.Layer

internal class OpGetLayerTransform(
    private val layer : Operation
) : OpTransformContext() {

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return (layer(value, variables, state) as Layer).transform
    }
}