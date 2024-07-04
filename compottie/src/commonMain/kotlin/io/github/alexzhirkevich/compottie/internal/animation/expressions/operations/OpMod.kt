package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpMod(
    private val a : Expression,
    private val b : Expression,
) : Expression {
    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        val a = a(property, variables, state)
        val b = b(property, variables, state)

        require(a is Number && b is Number) {
            "Can't get mod of $a and $b"
        }
        return a.toFloat().mod(b.toFloat())
    }
}