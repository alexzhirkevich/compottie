package io.github.alexzhirkevich.skriptie.ecmascript.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal class OpGetVariable<C : ScriptContext>(
    val name : String,
    val assignmentType : VariableType? = null
) : Expression<C> {

    override fun invoke(
        context: C,
    ): Any {
        return if (assignmentType != null) {
            context.setVariable(name, 0f, assignmentType)
        } else checkNotNull(context.getVariable(name)) {
            "Undefined variable: $name"
        }
    }
}