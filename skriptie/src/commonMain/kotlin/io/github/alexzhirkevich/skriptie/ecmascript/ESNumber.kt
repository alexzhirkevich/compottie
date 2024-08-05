package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.OpConstant
import io.github.alexzhirkevich.skriptie.common.defaults
import io.github.alexzhirkevich.skriptie.invoke

internal class ESNumber : ESFunctionBase("Number") {

    val isFinite by func("number"){
        val arg = it.getOrNull(0) ?: return@func false
        val num = toNumber(arg).toDouble()
        if (num.isNaN())
            return@func false
        num.isFinite()
    }

    val isInteger by func("number") {
        val arg = it.getOrNull(0) ?: return@func false
        val num = toNumber(arg)
        num is Long || num is Int || num is Short || num is Byte
    }

    val parseInt by func(
        FunctionParam("number"),
        "radix" defaults OpConstant(10L)
    ) {
        val arg = it.getOrNull(0) ?: return@func false
        val radix = it.getOrNull(1)?.let(::toNumber)
            ?.takeIf { !it.toDouble().isNaN() && it.toDouble().isFinite() }
            ?.toInt() ?: 10

        arg.toString().trim().trimParseInt(radix)
    }

    val isNan by func("number") {
        val arg = it.getOrNull(0) ?: return@func false
        toNumber(arg).toDouble().isNaN()
    }

    val isSafeInteger by func("number") {
        val arg = it.getOrNull(0) ?: return@func false
        val num = toNumber(arg)
        num is Long || num is Int || num is Short || num is Byte
    }

    val parseFloat by func("number") {
        val arg = it.getOrNull(0) ?: return@func false

        var dotCnt = 0
        val num = arg.toString().trim().takeWhile { c ->
            (c.isDigit() || c == '.' && dotCnt == 0).also {
                if (c == '.') dotCnt++
            }
        }
        num.toDoubleOrNull() ?: 0L
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

private fun String.trimParseInt(radix : Int) : Long? {
    println("$radix ${drop(2)}")

    return when (radix) {
        0 -> if (startsWith("0x",true)){
            trimParseInt(16)
        } else {
            trimParseInt(10)
        }
        10 -> takeWhile { it.isDigit() }.toLongOrNull()
        8 -> {
            takeWhile { it.isDigit() && it in '0'..'7' }
                .toLongOrNull(radix)
        }

        2 -> {
            takeWhile { it.isDigit() && it in '0'..'1' }
                .toLongOrNull(radix)
        }

        16 -> {
            if (!startsWith("0x") && !startsWith("0X"))
                null
            else {
                drop(2)
                    .takeWhile { (it in '0'..'9' || it.lowercaseChar() in 'a'..'f') }
                    .toLongOrNull(radix)
            }
        }
        else -> null
    }
}
