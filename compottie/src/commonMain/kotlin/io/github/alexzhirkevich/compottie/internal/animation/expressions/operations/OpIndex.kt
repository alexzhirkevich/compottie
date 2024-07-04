package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpIndex(
    private val v : Expression,
    private val idx : Expression?,
) : Expression {

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {

        val v = v(property, variables, state)
        val idx = idx?.invoke(property, variables, state) ?: return v

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