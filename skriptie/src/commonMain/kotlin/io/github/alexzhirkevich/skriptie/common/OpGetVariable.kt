package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.VariableType

internal class OpGetVariable<C : ScriptContext>(
    val name : String,
    val assignmentType : VariableType? = null
) : Expression<C> {

    override fun invoke(
        context: C,
    ): Any? {
        return if (assignmentType != null) {
            context.setVariable(name, 0f, assignmentType)
        } else {
            if (context.hasVariable(name)){
                context.getVariable(name)
            } else {
                Unit
            }
        }
    }
}