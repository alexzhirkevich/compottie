package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

internal class AddOperation(
    val a : Operation,
    val b : Operation
) : Operation {

    override fun invoke(value : Any, state: AnimationState): Any {
        val a = a(value, state)
        val b = b(value, state)

        return when {
            a is Float && b is Float -> a + b
            a is Vec2 && b is Vec2 -> a + b

            else -> error("Cant calculate the sum of $a and $b")
        }
    }
}