package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableScope

internal class OpAssign(
    val scope : VariableScope? = null,
    val variableName : String,
    val assignableValue : Expression,
    private val merge : ((Any, Any) -> Any)?
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Undefined {
        val v = assignableValue.invoke(property, context, state)

        val current = context.getVariable(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        context.setVariable(
            name = variableName,
            value = if (current != null && merge != null) {
                merge.invoke(current, v)
            } else v,
            scope = scope
        )

        return Undefined
    }
}