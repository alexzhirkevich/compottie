package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal interface ExpressionContext<T> : Expression {

    fun interpret(op: String, args: List<Expression>): Expression?

    fun withContext(
        block: T.(
            value: Any,
            variables: EvaluationContext,
            state: AnimationState
        ) -> Any
    ) = Expression { value, variables, state ->
        block(invoke(value, variables, state) as T, value, variables, state)
    }
}