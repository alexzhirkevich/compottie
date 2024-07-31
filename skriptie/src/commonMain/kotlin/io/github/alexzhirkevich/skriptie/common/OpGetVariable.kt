package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.Object

internal class OpGetVariable<C : ScriptContext>(
    val name : String,
    val receiver : Expression<C>?,
    val assignmentType : VariableType? = null
) : Expression<C> {

    override fun invoke(
        context: C,
    ): Any? {
        return if (assignmentType != null) {
            context.setVariable(name, 0f, assignmentType)
        } else {
            when (val res = receiver?.invoke(context)) {
                is Object -> if (name in res) res[name] else Unit

                else -> if (context.hasVariable(name)) {
                    context.getVariable(name)
                } else {
                    unresolvedReference(name)
                }
            }
        }
    }
}