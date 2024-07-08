package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpGetLayer(
    private val comp : Expression? = null,
    private val nameOrIndex : Expression? = null
) : OpLayerContext() {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (nameOrIndex == null) {
            state.layer
        } else {

            val n = nameOrIndex.invoke(property, context, state)

            val comp = comp?.invoke(property, context, state)
                ?: state.composition.expressionComposition

            require(comp is ExpressionComposition) {
                "Failed to cast $comp to Composition"
            }

            val layer = when (n) {
                is String -> comp.layersByName[n]
                is Number -> comp.layersByIndex[n.toInt()]
                else -> error("layer(.) takes string or number argument but got $n")
            }

            return checkNotNull(layer) {
                "Layer with name(index) '$nameOrIndex' wasn't found in composition ${comp.name}"
            }
        }
    }
}