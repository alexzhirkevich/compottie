package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpEquals(
    private val a : Expression,
    private val b : Expression,
    private val isTyped : Boolean
)  : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val a = a(property, context, state)
        val b = b(property, context, state)

        return when {
            isTyped || a::class == b::class -> a == b
            a is Number && b is Number -> a.toDouble() == b.toDouble()
            a is String && b is Number -> a.toDoubleOrNull() == b.toDouble()
            b is String && a is Number -> b.toDoubleOrNull() == a.toDouble()
            else -> a.toString() == b.toString()
        }
    }
}