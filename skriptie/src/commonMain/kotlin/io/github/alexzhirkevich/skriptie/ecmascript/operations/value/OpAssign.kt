package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal class OpAssign<C : ScriptContext>(
    val type : VariableType? = null,
    val variableName : String,
    val assignableValue : Expression<C>,
    private val merge : ((Any, Any) -> Any)?
) : Expression<C> {

    override fun invoke(context: C): Any {
        val v = assignableValue.invoke(context)

        val current = context.getVariable(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        val value = if (current != null && merge != null) {
            merge.invoke(current, v)
        } else v

        context.setVariable(
            name = variableName,
            value = value,
            type = type
        )

        return value
    }
}