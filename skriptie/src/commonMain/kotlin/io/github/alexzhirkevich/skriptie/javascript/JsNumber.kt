package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.argAtOrNull
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.ESClass
import io.github.alexzhirkevich.skriptie.ecmascript.ESObjectBase
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgsNotNull
import io.github.alexzhirkevich.skriptie.ecmascript.unresolvedReference
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal class JsNumberClass(
    val number: JsNumber
) : ESObjectBase("Number"), ESClass, JsWrapper<Number> by number, Comparable<JsWrapper<Number>> by number {

    override fun invoke(args: List<Expression>, context: ScriptRuntime): Any? = Unit

    override fun toString(): String {
        return number.toString()
    }

    override val name: String
        get() = "Number"

    override fun get(variable: Any?): Any? {
        return number[variable] ?: super.get(variable)
    }

    override val functions: List<Function> get() = emptyList()
    override val construct: Function? get() = null

    override val extends: Expression = Expression {
        it["Number"]
    }

    override val constructorClass: Expression? get() = extends
}

@JvmInline
internal value class JsNumber(
    override val value : Number
) : ESAny, JsWrapper<Number>, Comparable<JsWrapper<Number>> {

    override val type: String get() = "number"

    override fun get(variable: Any?): Any? {
        unresolvedReference(variable.toString())
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
                checkArgsNotNull(arguments, function)
                val digit = arguments.argAtOrNull(0)?.invoke(context)?.let(context::toNumber)?.toInt()
                value.toFixed(digit ?: 0)
            }
            "toPrecision" -> {
                checkArgsNotNull(arguments, function)
                val digit = arguments.argAtOrNull(0)?.invoke(context)?.let(context::toNumber)?.toInt()
                value.toPrecision(digit)
            }

            else -> super.invoke(function, context, arguments)
        }
    }

    override fun contains(variable: Any?): Boolean {
        return variable == "toFixed" || variable == "toPrecision"
    }

    override fun compareTo(other: JsWrapper<Number>): Int {
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
