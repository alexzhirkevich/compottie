package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation

internal class OpGetVariable(
    private val name : String
) : Operation{
    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return checkNotNull(variables[name]){
            "Undefined variable: $name"
        }
    }
}