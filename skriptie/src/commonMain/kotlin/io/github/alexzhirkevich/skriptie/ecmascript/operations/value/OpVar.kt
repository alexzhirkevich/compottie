package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.InterpretationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.OpGetVariable

internal class OpVar<C : ScriptContext>(
    val scope : VariableType
) : Expression<C>, InterpretationContext<C> {

    override fun interpret(callable: String?, args: List<Expression<C>>?): Expression<C>? {
        return if (callable == null)
            OpConstant(Undefined)
        else OpGetVariable(callable, assignmentType = scope)
    }

    override fun invoke(context: C) = Undefined
}