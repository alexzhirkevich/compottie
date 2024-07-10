package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpDot(
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
                a is List<*> && b is List<*> -> {
                    a as List<Number>
                    b as List<Number>
                    a[0].toFloat() * b[0].toFloat() + a[1].toFloat() * b[1].toFloat()
                }

                else -> error("Cant calculate the dot() of $a and $b")
            }
        }
    }
}