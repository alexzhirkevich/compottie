package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpConstant(value: Any?) =
    Expression {  value }

internal class OpGetVariable(
    val name : String,
    val receiver : Expression?,
    val assignmentType : VariableType? = null
) : Expression {

    override fun invokeRaw(context: ScriptRuntime, ): Any? {
        return if (assignmentType != null) {
            context.set(name, 0f, assignmentType)
        } else {
            when (val res = receiver?.invoke(context)) {
//                is JsObject<*> -> if (name in res) res[name] else Unit
                is ESAny -> res[name]

                else -> if (context.contains(name)) {
                    context.get(name)
                } else {
                    unresolvedReference(name)
                }
            }
        }
    }
}