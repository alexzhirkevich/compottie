package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import kotlin.math.pow
import kotlin.math.roundToInt

internal fun JsToPrecision(
    number : Expression,
    digits : Expression? = null
) = Expression { property, context, state ->

    val number = (number(property, context, state) as? Number?)?.toFloat()
        ?: unresolvedReference("toFixed")

    val digits = (digits?.invoke(property, context, state) as Number?)?.toInt()
        ?.takeIf { it > 0 }
        ?: return@Expression number

    number.roundTo(digits-1)
}

internal fun JsToFixed(
    number : Expression,
    digits : Expression?
) = Expression { property, context, state ->
    val number = (number(property, context, state) as? Number?)?.toFloat()
        ?: unresolvedReference("toFixed")

    val digits = (digits?.invoke(property, context, state) as Number?)?.toInt() ?: 0

    if (digits == 0) {
        return@Expression number.roundToInt().toString()
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
    (1..10).mapIndexed { i, it -> i to 10f.pow(it) }.toMap()
}

internal fun Float.roundTo(digit : Int) : Float {
    if(digit <= 0)
        return roundToInt().toFloat()

    val pow = pow10[digit-1] ?: return this
    return ((this * pow).roundToInt() / pow)
}