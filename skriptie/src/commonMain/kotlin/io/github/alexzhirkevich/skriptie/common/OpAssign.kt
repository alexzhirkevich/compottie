package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke

internal class OpAssign<C : ScriptRuntime>(
    val type : VariableType? = null,
    val variableName : String,
    val assignableValue : Expression<C>,
    private val merge : ((Any?, Any?) -> Any?)?
) : Expression<C> {

    override fun invokeRaw(context: C): Any? {
        val v = assignableValue.invoke(context)

        val current = context.getVariable(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        val value = if (current != null && merge != null) {
            merge.invoke(current, v)
        } else v

        context.setVariable(
            name = variableName,
            value = value,
            type = type
        )

        return value
    }
}