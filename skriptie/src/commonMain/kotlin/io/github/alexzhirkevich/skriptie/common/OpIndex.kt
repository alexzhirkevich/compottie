package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal class OpIndex<C : ScriptContext>(
    val variable : Expression<C>,
    val index : Expression<C>,
) : Expression<C> {

    override fun invoke(context: C): Any {
        val v = checkNotEmpty(variable(context))
        val idx = (index.invoke(context) as Number).toInt()

        return v.tryGet(idx)
    }
}