package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpUnaryPlus(
    private val v : Expression,
) : Expression {
    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {

        return when (val v = v(property, variables, state)) {
            is Number -> +v.toFloat()
            is Vec2 -> v
            else -> error("Cant apply unary plus to $v")
        }
    }
}