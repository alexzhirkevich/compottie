package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpClamp(
    private val v : Operation,
    private val from : Operation,
    private val to : Operation,
) : Operation {
    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        val v = v(value, variables, state)
        val from = from(value, variables, state)
        val to = to(value, variables, state)

        require(v is Number && from is Number && to is Number) {
            "Cant clamp ($v, $from, $to) : not a number"
        }

        return v.toFloat().coerceIn(from.toFloat(), to.toFloat(),)
    }
}