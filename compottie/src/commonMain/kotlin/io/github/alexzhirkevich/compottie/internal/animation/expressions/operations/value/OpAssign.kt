package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpAssign(
    private val variableName : String,
    private val assignableValue : Expression,
    private val merge : ((Any, Any) -> Any)?
) : Expression {


    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Undefined {
        val v = assignableValue.invoke(property, context, state)
        val current = context.variables[variableName]

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        context.variables[variableName] =  if (current != null && merge != null) {
            merge.invoke(current, v)
        } else v


        return Undefined
    }
}