package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke

internal class ESNumber : ESFunctionBase("Number") {

    init {
        init {
            "isFinite".func("number") {
                val arg = it.getOrNull(0) ?: return@func false
                val num = toNumber(arg).toDouble()
                if (num.isNaN())
                    return@func false
                num.isFinite()
            }
            "isInteger".func("number") {
                val arg = it.getOrNull(0) ?: return@func false
                val num = toNumber(arg)
                num is Long || num is Int || num is Short || num is Byte
            }
            "isNan".func("number") {
                val arg = it.getOrNull(0) ?: return@func false
                toNumber(arg).toDouble().isNaN()
            }
            "isSafeInteger".func("number") {
                val arg = it.getOrNull(0) ?: return@func false
                val num = toNumber(arg)
                num is Long || num is Int || num is Short || num is Byte
            }
            "parseFloat".func("number") {
                val arg = it.getOrNull(0) ?: return@func false

                var dotCnt = 0
                val num = arg.toString().trim().takeWhile { c ->
                    (c.isDigit() || c == '.' && dotCnt == 0).also {
                        if (c == '.') dotCnt++
                    }
                }
                num.toDoubleOrNull() ?: 0L
            }
            "parseInt".func("number") {
                println(it)
                val arg = it.getOrNull(0) ?: return@func false
                val radix = it.getOrNull(1)?.let(::toNumber)
                    ?.takeIf { !it.toDouble().isNaN() && it.toDouble().isFinite() }
                    ?.toInt() ?: 10

                arg.toString().trim().trimParseInt(radix)
            }
        }
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
        10 -> takeWhile { it.isDigit() }.toLongOrNull()
        8 -> {
            if (!startsWith("0o") && !startsWith("0O"))
                null
            else drop(2)
                .takeWhile { it.isDigit() && it in '0'..'7' }
                .toLongOrNull(radix)
        }

        2 -> {
            if (!startsWith("0b") && !startsWith("0B"))
                null
            else drop(2)
                .takeWhile { it.isDigit() && it in '0'..'1' }
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
