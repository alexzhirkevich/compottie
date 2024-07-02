package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpSub(
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
        operator fun invoke(a: Any, b: Any): Any {
            return when {
                a is Float && b is Float -> a - b
                a is Vec2 && b is Vec2 -> a - b
                else -> try {
                    a.toString().toFloat() - b.toString().toFloat()
                } catch (t: Throwable) {
                    error("Cant subtract $b from $a")
                }
            }
        }
    }
}