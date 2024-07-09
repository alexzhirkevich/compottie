package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssign

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

    fun withTimeRemapping(
        timeRemapping : Expression?,
        block: T.(
            value: Any,
            variables: EvaluationContext,
            state: AnimationState
        ) -> Any
    ) = Expression { value, variables, state ->

        val v = invoke(value, variables, state) as T

        if (timeRemapping == null || timeRemapping is OpGetTime) {
            block(v, value, variables, state)
        } else {
            state.onTime((timeRemapping.invoke(value, variables, state) as Number).toFloat()) {
                block(v, value, variables, it)
            }
        }
    }
}

internal fun List<Expression>.getForNameOrIndex(
    index : Int,
    vararg name : String,
) : Expression? {

    forEach { op ->
        if (op is OpAssign && name.any { op.variableName == it } ) {
            return op.assignableValue
        }
    }

    return getOrNull(index)
}