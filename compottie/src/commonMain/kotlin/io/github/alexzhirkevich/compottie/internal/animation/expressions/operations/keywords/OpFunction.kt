package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import androidx.compose.ui.util.fastForEachIndexed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference

internal class FunctionParam(
    val name : String,
    val default : Expression?
)

internal class OpFunction(
    val name : String,
    private val parameters : List<FunctionParam>,
    private val body : Expression
) {
    private val arguments = mutableMapOf<String, Any>()

    fun invoke(
        args: List<Expression>,
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        arguments.clear()
        parameters.fastForEachIndexed { i, p ->
            arguments[p.name] = requireNotNull(args.argForNameOrIndex(i, p.name) ?: p.default) {
                "'${p.name}' argument of '$name' function is missing"
            }.invoke(property, context, state)
        }

        return context.withScope(
            extraVariables = arguments
        ){
            body.invoke(property, it, state)
        }
    }
}

internal fun OpFunctionExec(
    name : String,
    parameters : List<Expression>,
) = Expression { property, context, state ->
    val function = context.getFunction(name) ?: unresolvedReference(name)

    function.invoke(
        args = parameters,
        property = property,
        context = context,
        state = state
    )
}
