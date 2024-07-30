package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.fastForEachIndexed

internal class FunctionParam<C : ScriptContext>(
    val name : String,
    val default : Expression<C>?
)

internal class OpFunction<C : ScriptContext>(
    val name : String,
    private val parameters : List<FunctionParam<C>>,
    private val body : Expression<C>
) {
    private val arguments = mutableMapOf<String, Pair<VariableType, Any>>()

    fun invoke(
        args: List<Expression<C>>,
        context: C,
    ): Any {
        arguments.clear()
        parameters.fastForEachIndexed { i, p ->
            arguments[p.name] = Pair(
                VariableType.Let,
                requireNotNull(args.argForNameOrIndex(i, p.name) ?: p.default) {
                    "'${p.name}' argument of '$name' function is missing"
                }.invoke(context)
            )
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
