package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpMul(
    private val a : Expression,
    private val b : Expression,
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        return invoke(
            a(property, context, state),
            b(property, context, state)
        )
    }

    companion object {
        operator fun invoke(a : Any, b : Any) : Any {
            return when {
                (a is Number && b is Number) -> a.toFloat() * b.toFloat()
                (a is Vec2 && b is Number) -> a * b.toFloat()
                (a is Number && b is Vec2) -> b * a.toFloat()
                else -> error("Cant multiply $a by $b")
            }
        }
    }
}