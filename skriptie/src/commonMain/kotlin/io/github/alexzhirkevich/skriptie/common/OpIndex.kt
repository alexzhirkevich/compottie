package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.number

internal class OpIndex<C : ScriptRuntime>(
    val variable : Expression<C>,
    val index : Expression<C>,
) : Expression<C> {

    override fun invokeRaw(context: C): Any {
        return invoke(context, variable, index)
    }

    companion object {
        fun <C : ScriptRuntime> invoke(context: C, variable : Expression<C>, index : Expression<C>) : Any{
            val v = checkNotEmpty(variable(context))
            val idx = (index(context).number()).toInt()

            return v.tryGet(idx)
        }
    }
}