package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal class OpGetVariable(
    val name : String,
    val assignmentType : VariableType? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (assignmentType != null) {
            context.setVariable(name, 0f, assignmentType)
        } else checkNotNull(context.getVariable(name)) {
            "Undefined variable: $name"
        }
    }
}