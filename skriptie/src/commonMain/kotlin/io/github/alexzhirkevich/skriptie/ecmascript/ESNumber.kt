package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.numberOrNull

internal class ESNumber : ESFunctionBase("Number") {

    init {
        setFunction(
            "isFinite".func("number") {
                val num = it.getOrNull(0)?.numberOrNull() ?: return@func false
                num.toDouble().isFinite()
            }
        )
    }

    override fun get(variable: String): Any? {
        return when (variable) {
            "EPSILON" -> Double.MIN_VALUE
            "length" -> Double.MIN_VALUE
            "MAX_SAFE_INTEGER" -> Long.MAX_VALUE
            "MAX_VALUE" -> Double.MAX_VALUE
            "MIN_SAFE_INTEGER" -> Long.MIN_VALUE
            "NaN" -> Double.NaN
            "NEGATIVE_INFINITY" -> Double.NEGATIVE_INFINITY
            "POSITIVE_INFINITY" -> Double.POSITIVE_INFINITY
            else -> super.get(variable)
        }
    }

    override fun invoke(args: List<Expression>, context: ScriptRuntime): Any? {
        return context.toNumber(args.single().invoke(context))
    }
}

