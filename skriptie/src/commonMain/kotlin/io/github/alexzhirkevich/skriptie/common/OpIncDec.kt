package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpIncDecAssign(
    variable: Expression,
    preAssign : Boolean,
    op: (Any?) -> Any?
) : Expression {

    val value = Expression { op(variable(it)) }
    val assignment = when {
        variable is OpGetVariable && variable.assignmentType == null ->
            OpAssign(
                variableName = variable.name,
                assignableValue = value,
                merge = null
            )

        variable is OpIndex && variable.variable is OpGetVariable ->
            OpAssignByIndex(
                variableName = variable.variable.name,
                index = variable.index,
                assignableValue = value,
                scope = null,
                merge = null
            )

        else -> error("$variable is not assignable")
    }

    if (preAssign) {
        return assignment
    }

    return Expression { ctx ->
        variable(ctx).also { assignment(ctx) }
    }
}
