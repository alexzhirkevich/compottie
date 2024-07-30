package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssign
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssignByIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpGetVariable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpIndex

internal fun Expression.isAssignable() : Boolean {
    return this is OpGetVariable && assignmentType == null ||
            this is OpIndex && variable is OpGetVariable
}

internal val Expression.nameForAssignment : String
    get() = when {
        this is OpGetVariable && assignmentType == null -> name
        this is OpIndex && variable is OpGetVariable -> variable.name
        else -> error("Expression is not left assignable")
    }

internal fun OpIncrement(variable : Expression) : Expression = OpIncDec(
    variable, ::increment, "increment"
)

internal fun OpDecrement(variable : Expression) : Expression = OpIncDec(
    variable, ::decrement, "decrement"
)


private fun increment(v : Any) : Any {
    return when (val v = v.validateJsNumber()) {
        is Long -> v + 1
        is Double -> v + 1
        is Number -> v.toDouble() + 1
        else -> error("can't increment $v")
    }
}

private fun decrement(v : Any) : Any {
    return when (val v = v.validateJsNumber()) {
        is Long -> v - 1
        is Double -> v - 1
        is Number -> v.toDouble() - 1
        else -> error("can't decrement $v")
    }
}

private fun OpIncDec(
    variable: Expression,
    op: (Any) -> Any, name : String) : Expression {
    return when {
        variable is OpGetVariable && variable.assignmentType == null ->
            OpAssign(
                variableName = variable.name,
                assignableValue = { property, context, state ->
                    op(variable(property, context, state))
                },
                merge = null
            )
        variable is OpIndex && variable.variable is OpGetVariable ->
            OpAssignByIndex(
                variableName = variable.variable.name,
                index = variable.index,
                assignableValue = { property, context, state ->
                    op(variable(property, context, state))
                },
                scope = null,
                merge = null
            )
        else -> error("Can't $name $variable")
    }
}
