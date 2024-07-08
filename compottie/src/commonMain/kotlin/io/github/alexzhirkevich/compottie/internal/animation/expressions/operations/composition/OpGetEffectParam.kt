package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.toExpressionType
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect

internal class OpGetEffectParam(
    private val effect : Expression,
    private val nameOrIndex : Expression,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        val effect = effect(property, context, state) as LayerEffect

        val value = when (val nameOrIndex = nameOrIndex(property, context, state)) {
            is String -> effect.valueByName[nameOrIndex]
            is Number -> effect.valueByIndex[nameOrIndex.toInt()]
            else -> error("Effect.value(.) can take string (name) or number(index) but $nameOrIndex got")
        }

        return checkNotNull(value?.value) {
            "Effect value with name or index '$nameOrIndex' wasn't found in Effect"
        }.raw(state)
    }
}