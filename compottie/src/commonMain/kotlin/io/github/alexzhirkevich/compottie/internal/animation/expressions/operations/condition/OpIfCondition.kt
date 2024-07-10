package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.condition

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant

internal class OpIfCondition(
    val condition : Expression = OpConstant(true),
    var onTrue : Expression? = null,
    var onFalse : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Undefined {
        val expr = if (condition(property, context, state) as Boolean){
            onTrue
        } else {
            onFalse
        }

        expr?.invoke(property, context, state)

        return Undefined
    }
}