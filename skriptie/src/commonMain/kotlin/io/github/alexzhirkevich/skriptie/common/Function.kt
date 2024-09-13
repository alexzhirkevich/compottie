package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.argForNameOrIndex
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.ESObject
import io.github.alexzhirkevich.skriptie.ecmascript.ESObjectBase
import io.github.alexzhirkevich.skriptie.ecmascript.SyntaxError
import io.github.alexzhirkevich.skriptie.ecmascript.TypeError
import io.github.alexzhirkevich.skriptie.ecmascript.unresolvedReference
import io.github.alexzhirkevich.skriptie.invoke

public class FunctionParam(
    public val name : String,
    public val isVararg : Boolean = false,
    public val default : Expression? = null
)

internal infix fun String.defaults(default: Expression?) : FunctionParam {
    return FunctionParam(this, false, default)
}


internal interface Callable {
    operator fun invoke(args: List<Expression>, context: ScriptRuntime) : Any?
}


internal class Function(
    override val name : String,
    val parameters : List<FunctionParam>,
    val body : Expression,
    var thisRef : Any? = null,
    val isClassMember : Boolean = false,
    var isStatic : Boolean = false,
    val extraVariables: Map<String, Pair<VariableType, Any?>> = emptyMap(),
) : ESObject by ESObjectBase(name), Callable, Named {
    override val type: String
        get() = "function"

    fun copy(
        body: Expression = this.body,
        extraVariables: Map<String, Pair<VariableType, Any?>> = emptyMap(),
    ) : Function{
        return Function(
            name = name,
            parameters = parameters,
            body = body,
            thisRef = thisRef,
            isClassMember = isClassMember,
            extraVariables = this.extraVariables + extraVariables
        )
    }

    init {
        val varargs = parameters.count { it.isVararg }

        if (varargs > 1 || varargs == 1 && !parameters.last().isVararg){
            throw SyntaxError("Rest parameter must be last formal parameter")
        }
    }

    override fun invoke(
        args: List<Expression>,
        context: ScriptRuntime,
    ): Any? {
        return try {
            val arguments = buildMap {
                parameters.fastForEachIndexed { i, p ->
                    val value = if (p.isVararg) {
                        args.drop(i).fastMap { it(context) }
                    } else {
                        (args.argForNameOrIndex(i, p.name) ?: p.default)
                            ?.invoke(context)
                            ?: Unit
                    }
                    this[p.name] = VariableType.Local to value
                }
                thisRef?.let {
                    this["this"] = VariableType.Const to it
                }
            }
            context.withScope(arguments + extraVariables, body::invoke)
        } catch (ret: BlockReturn) {
            ret.value
        }
    }
}

internal class OpFunctionExec(
    override val name : String,
    val receiver : Expression?,
    val parameters : List<Expression>,
) : Expression, Named {
    override fun invokeRaw(context: ScriptRuntime): Any? {
        val res = receiver?.invoke(context)
        val function = when {
            res == null -> context[name]
            res is Callable && name.isBlank() -> res
            res is ESObject -> res[name]
            res is ESAny -> {
                return res.invoke(name, context, parameters)
            }

            else -> null
        }
        if (function is Unit) {
            unresolvedReference(name)
        }
        if (function !is Callable) {
            throw TypeError("$name ($function) is not a function")
        }
        return function.invoke(
            args = parameters,
            context = context,
        )
    }
}

