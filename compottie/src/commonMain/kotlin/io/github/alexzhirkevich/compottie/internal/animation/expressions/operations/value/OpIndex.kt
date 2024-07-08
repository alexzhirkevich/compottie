package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.get

internal class OpIndex(
    val variable : Expression,
    val index : Expression,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        val v = variable(property, context, state)
        val idx = (index.invoke(property, context, state) as Number).toInt()

        return v[idx]
    }
}