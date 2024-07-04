package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpGetLayer(
    private val comp : Expression? = null,
    private val name : Expression? = null
) : OpLayerContext() {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (name == null) {
            state.layer
        } else {

            val n = name.invoke(property, context, state) as String

            val comp = comp?.invoke(property, context, state)
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