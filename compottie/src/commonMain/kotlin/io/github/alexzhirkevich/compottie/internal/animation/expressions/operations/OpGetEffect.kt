package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.layers.Layer

internal class OpGetEffect(
    private val layer : Expression,
    private val nameOrIndex : Expression
) : OpEffectContext() {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): LayerEffect {

        val layer = layer(property, context, state) as Layer

        return when (val nameOrIndex = nameOrIndex(property, context, state)) {
            is String -> checkNotNull(layer.effects.firstOrNull { it.name == nameOrIndex }) {
                "Effect with name $nameOrIndex wasn't found for layer ${layer.name}"
            }

            is Number -> checkNotNull(layer.effects.firstOrNull { it.index == nameOrIndex.toInt() }) {
                "Effect with index $nameOrIndex wasn't found for layer ${layer.name}"
            }

            else -> error("effect(.) argument must be number|string bug got $nameOrIndex")
        }
    }
}