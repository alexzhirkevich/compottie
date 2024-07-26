package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableScope

internal class OpVar(
    val scope : VariableScope
) : Expression, ExpressionContext<Any> {

    override fun interpret(callable: String?, args: List<Expression>?): Expression {
        return if (callable == null)
            Expression.UndefinedExpression
        else OpGetVariable(callable, assignInScope = scope)
    }

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ) = Undefined
}