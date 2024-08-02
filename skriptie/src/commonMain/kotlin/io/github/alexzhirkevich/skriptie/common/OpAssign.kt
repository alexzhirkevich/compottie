package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke

internal class OpAssign(
    val type : VariableType? = null,
    val variableName : String,
    val assignableValue : Expression,
    private val merge : ((Any?, Any?) -> Any?)?
) : Expression {

    override fun invokeRaw(context: ScriptRuntime): Any? {
        val v = assignableValue.invoke(context)

        val current = context.get(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        val value = if (current != null && merge != null) {
            merge.invoke(current, v)
        } else v

        context.set(
            variable = variableName,
            value = value,
            type = type
        )

        return value
    }
}