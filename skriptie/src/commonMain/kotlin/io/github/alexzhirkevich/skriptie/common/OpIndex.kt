package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.number

internal class OpIndex(
    val variable : Expression,
    val index : Expression,
) : Expression {

    override fun invokeRaw(context: ScriptRuntime): Any {
        return invoke(context, variable, index)
    }

    companion object {
        fun  invoke(context: ScriptRuntime, variable : Expression, index : Expression) : Any{
            val v = checkNotEmpty(variable(context))
            val idx = (index(context).number()).toInt()

            return v.valueAtIndexOrUnit(idx)
        }
    }
}