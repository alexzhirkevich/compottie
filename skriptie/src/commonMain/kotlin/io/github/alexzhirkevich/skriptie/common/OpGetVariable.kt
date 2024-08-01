package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.invoke

internal fun <C : ScriptRuntime> OpConstant(value: Any?) =
    Expression<C> {  value }

internal class OpGetVariable<C : ScriptRuntime>(
    val name : String,
    val receiver : Expression<C>?,
    val assignmentType : VariableType? = null
) : Expression<C> {

    override fun invokeRaw(context: C, ): Any? {
        return if (assignmentType != null) {
            context.setVariable(name, 0f, assignmentType)
        } else {
            when (val res = receiver?.invoke(context)) {
//                is JsObject<*> -> if (name in res) res[name] else Unit
                is ESAny<*> -> res[name]

                else -> if (context.hasVariable(name)) {
                    context.getVariable(name)
                } else {
                    unresolvedReference(name)
                }
            }
        }
    }
}