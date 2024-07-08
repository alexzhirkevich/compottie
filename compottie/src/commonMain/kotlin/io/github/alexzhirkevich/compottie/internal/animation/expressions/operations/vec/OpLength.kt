package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.hypot
import kotlin.math.sqrt

internal class OpLength(
    private val a : Expression,
    private val b : Expression? = null,
) : Expression {


    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return invoke(
            a(property, context, state),
            b?.invoke(property, context, state)
        )
    }

    companion object {

        operator fun invoke(a : Any, b : Any? = null) : Any {
            return when {
                a is List<*> && b == null -> hypot((a[0] as Number).toFloat(), (a[1] as Number).toFloat())
                a is List<*> && b is List<*> -> {
                    a as List<Number>
                    b as List<Number>
                    hypot(b[0].toFloat() - a[0].toFloat(), b[1].toFloat() - a[1].toFloat())
                }

                else -> error("Cant calculate the normalize() of $a and $b")
            }
        }
    }
}