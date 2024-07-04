package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpClamp(
    private val v : Expression,
    private val from : Expression,
    private val to : Expression,
) : Expression {
    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        val v = v(property, variables, state)
        val from = from(property, variables, state)
        val to = to(property, variables, state)

        require(v is Number && from is Number && to is Number) {
            "Cant clamp ($v, $from, $to) : not a number"
        }

        return v.toFloat().coerceIn(from.toFloat(), to.toFloat(),)
    }
}