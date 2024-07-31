package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.VariableType

internal class OpVar<C : ScriptContext>(
    val scope : VariableType
) : Expression<C>, InterpretationContext<C> {

    override fun interpret(callable: String?, args: List<Expression<C>>?): Expression<C>? {
        return if (callable == null) {
            OpConstant(Unit)
        } else {
            OpGetVariable(
                name = callable,
                receiver = null,
                assignmentType = scope
            )
        }
    }
}

internal fun <C : ScriptContext> OpConstant(value: Any?) =
    Expression<C> {  value }