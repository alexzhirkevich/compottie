package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.tryGet

internal class OpIndex<C : ScriptContext>(
    val variable : Expression<C>,
    val index : Expression<C>,
) : Expression<C> {

    override fun invoke(context: C): Any {
        val v = variable(context)
        val idx = (index.invoke(context) as Number).toInt()

        return v.tryGet(idx) ?: Undefined
    }
}