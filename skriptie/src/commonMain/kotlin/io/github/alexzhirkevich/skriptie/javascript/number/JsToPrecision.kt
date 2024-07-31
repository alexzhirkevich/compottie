package io.github.alexzhirkevich.skriptie.javascript.number

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.common.checkNotEmpty
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext
import io.github.alexzhirkevich.skriptie.javascript.numberOrThis
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal fun JsToPrecision(
    number : Expression<JSScriptContext>,
    digits : Expression<JSScriptContext>? = null
) = Expression<JSScriptContext> { context ->

    val number = checkNotEmpty(number(context)?.numberOrThis() as? Number?).toDouble()

    val digits = (digits?.invoke(context)?.numberOrThis() as Number?)?.toInt()
        ?.takeIf { it > 0 }
        ?: return@Expression number

    number.roundTo(digits-1)
}

internal fun JsToFixed(
    number : Expression<JSScriptContext>,
    digits : Expression<JSScriptContext>?
) = Expression<JSScriptContext> { context ->
    val number = checkNotEmpty(number(context)?.numberOrThis() as? Number?).toDouble()

    val digits = (digits?.invoke(context)?.numberOrThis() as Number?)?.toInt() ?: 0

    if (digits == 0) {
        return@Expression number.roundToLong().toString()
    }

    val stringNumber = number.roundTo(digits).toString()

    val intPart = stringNumber.substringBefore(".")
    val floatPart = stringNumber.substringAfter(".", "").take(digits)

    if (floatPart.isBlank()) {
        return@Expression intPart
    }

    (intPart + "." + floatPart.padEnd(digits, '0'))
}

private val pow10 by lazy {
    (1..10).mapIndexed { i, it -> i to 10.0.pow(it) }.toMap()
}

internal fun Double.roundTo(digit : Int) : Double {
    if(digit <= 0)
        return roundToLong().toDouble()

    val pow = pow10[digit-1] ?: return this
    return ((this * pow).roundToInt() / pow)
}
