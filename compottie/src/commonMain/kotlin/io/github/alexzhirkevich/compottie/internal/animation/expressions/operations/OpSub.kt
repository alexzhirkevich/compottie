package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpSub(
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