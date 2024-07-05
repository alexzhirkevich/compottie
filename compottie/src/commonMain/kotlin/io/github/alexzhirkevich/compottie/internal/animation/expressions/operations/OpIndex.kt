package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpIndex(
    val variable : Expression,
    val index : Expression,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        val v = variable(property, context, state)
        val idx = index.invoke(property, context, state)

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