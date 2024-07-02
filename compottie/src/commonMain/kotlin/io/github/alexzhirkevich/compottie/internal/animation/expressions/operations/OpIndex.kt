package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpIndex(
    private val v : Operation,
    private val idx : Operation?,
) : Operation {

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {

        val v = v(value, variables, state)
        val idx = idx?.invoke(value, variables, state) ?: return v

        return when (v) {
            is Vec2 -> when (idx) {
                0, 0L, 0.0, 0.0f -> v.x
                1, 1L, 1.0, 1.0f  -> v.y
                else -> error("Cant get $idx index of Vec2")
            }

            else -> error("Cant get value by index ($idx) from $v")
        }
    }
}