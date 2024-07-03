package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpMul(
    private val a : Expression,
    private val b : Expression,
) : Expression {
    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {

        return invoke(
            a(property, variables, state),
            b(property, variables, state)
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