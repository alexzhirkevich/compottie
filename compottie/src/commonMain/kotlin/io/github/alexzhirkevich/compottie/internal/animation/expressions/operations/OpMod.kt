package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpMod(
    private val a : Operation,
    private val b : Operation,
) : Operation {
    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        val a = a(value, variables, state)
        val b = b(value, variables, state)

        require(a is Number && b is Number) {
            "Can't get mod of $a and $b"
        }
        return a.toFloat().mod(b.toFloat())
    }
}