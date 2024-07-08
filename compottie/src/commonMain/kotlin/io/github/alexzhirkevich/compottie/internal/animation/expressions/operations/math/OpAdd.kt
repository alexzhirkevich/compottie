package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.min

internal class OpAdd(
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

        operator fun invoke(a: Any, b: Any): Any {
            return when {
                a is Number && b is Number -> a.toFloat() + b.toFloat()
                a is List<*> && b is List<*> -> {
                    a as List<Number>
                    b as List<Number>

                    return List(min(a.size, b.size)) {
                        a[it].toFloat() + b[it].toFloat()
                    }
                }

                a is List<*> && b is Number -> (a as List<Number>).first().toFloat() + b.toFloat()
                a is Number && b is List<*> -> a.toFloat() + (b as List<Number>).first().toFloat()

                else -> error("Cant calculate the sum of $a and $b")
            }
        }
    }
}

internal operator fun Any.plus(other : Any) : Any = OpAdd.invoke(this, other)