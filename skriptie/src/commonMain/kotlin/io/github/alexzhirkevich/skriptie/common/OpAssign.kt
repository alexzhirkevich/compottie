package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.ESObject
import io.github.alexzhirkevich.skriptie.invoke

internal class OpAssign(
    val type : VariableType? = null,
    val variableName : String,
    val receiver : Expression?=null,
    val assignableValue : Expression,
    private val merge : ((Any?, Any?) -> Any?)?
) : Expression {

    override fun invokeRaw(context: ScriptRuntime): Any? {
        val v = assignableValue.invoke(context)
        val r = receiver?.invoke(context)

        val current = if (receiver == null) {
            context[variableName]
        } else {
            when (r){
                is ESAny -> r[variableName]
                else -> null
            }
        }

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        val value = if (current != null && merge != null) {
            merge.invoke(current, v)
        } else v

        if (receiver == null) {
            context.set(
                variable = variableName,
                value = value,
                type = type
            )
        } else {
            when (r) {
                is ESObject -> r[variableName] = value
                else -> throw TypeError("Cannot set properties of ${if (r == Unit) "undefined" else r} (setting '$variableName')")
            }
        }

        return value
    }
}