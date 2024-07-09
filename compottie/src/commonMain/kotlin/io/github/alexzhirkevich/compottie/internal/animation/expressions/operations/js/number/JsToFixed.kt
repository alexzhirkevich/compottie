package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import kotlin.math.roundToInt

internal class JsToFixed(
    private val number : Expression,
    private val digits : Expression?
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {

        val number = (number(property, context, state) as? Number?)?.toFloat()
            ?: unresolvedReference("toFixed")

        val digits = (digits?.invoke(property, context, state) as Number?)?.toInt() ?: 0

        if (digits == 0) {
            return number.roundToInt().toString()
        }

        val stringNumber = number.roundTo(digits).toString()

        val intPart = stringNumber.substringBefore(".")
        val floatPart = stringNumber.substringAfter(".", "").take(digits)

        if (floatPart.isBlank()) {
            return intPart
        }

        return (intPart + "." + floatPart.padEnd(digits, '0'))
    }
}