package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.argForNameOrIndex
import io.github.alexzhirkevich.skriptie.ecmascript.Object

internal class FunctionParam<C : ScriptContext>(
    val name : String,
    val isVararg : Boolean = false,
    val default : Expression<C>? = null
)

internal infix fun <C : ScriptContext> String.with(default: Expression<C>?) : FunctionParam<C> {
    return FunctionParam(this, false, default)
}


internal class OpFunction<C : ScriptContext>(
    val name : String,
    private val parameters : List<FunctionParam<C>>,
    private val body : Expression<C>
) {
    init {
        val varargs = parameters.count { it.isVararg }

        if (varargs > 1 || varargs == 1 && !parameters.last().isVararg){
            throw SyntaxError("Rest parameter must be last formal parameter")
        }
    }

    fun invoke(
        args: List<Expression<C>>,
        context: C,
    ): Any? {
        try {
            val arguments = buildMap {
                parameters.fastForEachIndexed { i, p ->
                    val value = if (p.isVararg){
                        args.drop(i).fastMap { it(context) }
                    } else {
                        requireNotNull(args.argForNameOrIndex(i, p.name) ?: p.default) {
                            "'${p.name}' argument of '$name' function is missing"
                        }.invoke(context)
                    }
                    this[p.name] = Pair(
                        VariableType.Local,
                        value
                    )
                }
            }
            return context.withScope(arguments) {
                body.invoke(it as C)
            }
        } catch (ret: BlockReturn) {
            return ret.value
        }
    }
}

internal fun <C : ScriptContext> OpFunctionExec(
    name : String,
    receiver : Expression<C>?,
    parameters : List<Expression<C>>,
) = Expression<C> { ctx ->

    val function = when (val res = receiver?.invoke(ctx)) {
        null -> ctx.getVariable(name)
        is Object -> res[name]
        else -> null
    } as? OpFunction<C> ?: unresolvedReference(name)

    function.invoke(
        args = parameters,
        context = ctx,
    )
}
