package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal fun <C: ScriptContext> OpIncDecAssign(
    variable: Expression<C>,
    preAssign : Boolean,
    op: (Any?) -> Any?
) : Expression<C> {

    val value = Expression<C> { op(variable(it)) }
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
