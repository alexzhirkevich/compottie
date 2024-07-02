package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpAdd(
    private val a : Operation,
    private val b : Operation,
) : Operation {
    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return invoke(
            a(value, variables, state),
            b(value, variables, state)
        )
    }

    companion object {
        operator fun invoke(a : Any, b : Any) : Any {
            return when {
                a is Number && b is Number -> a.toFloat() + b.toFloat()
                a is Vec2 && b is Vec2 -> a + b
                a is Vec2 && b is Number -> a.x + b.toFloat()
                a is Number && b is Vec2 -> a.toFloat() + b.x

                else -> error("Cant calculate the sum of $a and $b")
            }
        }
    }
}