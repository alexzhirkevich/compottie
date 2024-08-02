package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.argAtOrNull
import io.github.alexzhirkevich.skriptie.common.unresolvedReference
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgsNotNull
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@JvmInline
public value class JsNumber(
    override val value : Number
) : ESAny, JsWrapper<Number>, Comparable<JsNumber> {

    override val type: String get() = "number"

    override fun get(property: String): Any? {
        unresolvedReference(property)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        return when(function){
            "toFixed" -> {
                println(arguments)
                checkArgsNotNull(arguments, function)
                val digit = arguments.argAtOrNull(0)?.invoke(context)?.number()?.toInt()
                value.toFixed(digit ?: 0)
            }
            "toPrecision" -> {
                checkArgsNotNull(arguments, function)
                val digit = arguments.argAtOrNull(0)?.invoke(context)?.number()?.toInt()
                value.toPrecision(digit)
            }

            else -> super.invoke(function, context, arguments)
        }
    }

    override fun compareTo(other: JsNumber): Int {
        return value.toDouble().compareTo(other.value.toDouble())
    }
}

private fun Number.toPrecision(digits: Int?) : Double {

    if (digits == null){
        return toDouble()
    }
    return toDouble().roundTo(digits-1)
}

private fun Number.toFixed(digits: Int) : String {

    if (digits == 0) {
        return toDouble().roundToLong().toString()
    }

    val stringNumber = toDouble().roundTo(digits).toString()

    val intPart = stringNumber.substringBefore(".")
    val floatPart = stringNumber.substringAfter(".", "").take(digits)

    if (floatPart.isBlank()) {
        return intPart
    }

    return (intPart + "." + floatPart.padEnd(digits, '0'))
}

private val pow10 by lazy {
    (1..10).mapIndexed { i, it -> i to 10.0.pow(it) }.toMap()
}

private fun Double.roundTo(digit : Int) : Double {
    if(digit <= 0)
        return roundToLong().toDouble()

    val pow = pow10[digit-1] ?: return this
    return ((this * pow).roundToInt() / pow)
}
