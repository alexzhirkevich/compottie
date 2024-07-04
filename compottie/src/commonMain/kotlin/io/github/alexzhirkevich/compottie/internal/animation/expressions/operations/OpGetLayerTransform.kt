package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.layers.Layer

internal class OpGetLayerTransform(
    private val layer : Expression
) : OpTransformContext() {

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        return (layer(property, variables, state) as Layer).transform
    }
}