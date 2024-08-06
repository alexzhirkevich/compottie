package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline

@JvmInline
internal value class OpConstant(val value: Any?) : Expression {
    override fun invokeRaw(context: ScriptRuntime): Any? {
        return value
    }
}

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
                is ESAny -> res[name]
                else ->
                    if (name in context) {
                        context[name]
                    } else {
                        unresolvedReference(name)
                    }
            }
        }
    }
}