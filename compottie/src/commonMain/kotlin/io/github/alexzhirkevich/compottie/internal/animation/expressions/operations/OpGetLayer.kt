package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpGetLayer(
    private val comp : Expression? = null,
    private val name : Expression? = null
) : OpLayerContext() {

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        return if (name == null) {
            state.layer
        } else {

            val n = name.invoke(property, variables, state) as String

            val comp = comp?.invoke(property, variables, state)
                ?: state.composition.expressionComposition

            require(comp is ExpressionComposition){
                "Failed to cast $comp to Composition"
            }
            return checkNotNull(comp.layers[n]) {
                "Layer with name '$name' wasn't found in composition ${comp.name}"
            }
        }
    }
}