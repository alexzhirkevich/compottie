package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import kotlin.math.pow
import kotlin.math.roundToInt

internal class JsToPrecision(
    private val number : Expression,
    private val digits : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {

        val number = (number(property, context, state) as? Number?)?.toFloat()
            ?: unresolvedReference("toFixed")

        val digits = (digits?.invoke(property, context, state) as Number?)?.toInt() ?: 0

        return invoke(number, digits)
    }

    companion object {
        fun invoke(number : Float, precision: Int) : Float {
            return number.roundTo(precision)
        }
    }
}
private val pow10 by lazy {
    (1..10).mapIndexed { i, it -> i to 10f.pow(it) }.toMap()
}

internal fun Float.roundTo(digit : Int) : Float {
    if(digit == 0)
        return roundToInt().toFloat()
    val pow = pow10[digit] ?: return this
    return (this * pow).roundToInt() / pow
}