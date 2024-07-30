package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.argForNameOrIndex

internal class FunctionParam<C : ScriptContext>(
    val name : String,
    val default : Expression<C>?
)

internal class OpFunction<C : ScriptContext>(
    val name : String,
    private val parameters : List<FunctionParam<C>>,
    private val body : Expression<C>
) {

    fun invoke(
        args: List<Expression<C>>,
        context: C,
    ): Any? {
        val arguments = buildMap {
            parameters.fastForEachIndexed { i, p ->
                this[p.name] = Pair(
                    VariableType.Local,
                    requireNotNull(args.argForNameOrIndex(i, p.name) ?: p.default) {
                        "'${p.name}' argument of '$name' function is missing"
                    }.invoke(context)
                )
            }
        }

        return context.withScope(arguments){
            body.invoke(it as C)
        }
    }
}

internal fun <C : ScriptContext> OpFunctionExec(
    name : String,
    parameters : List<Expression<C>>,
) = Expression<C> {
    val function = it.getVariable(name) as? OpFunction<C>
        ?: unresolvedReference(name)

    function.invoke(
        args = parameters,
        context = it,
    )
}

internal class OpReturn<C : ScriptContext>(
    val value : Expression<C>
) : Expression<C> by value