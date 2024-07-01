package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal class GetValueOperation : Operation {
    override fun invoke(value: Any, state: AnimationState): Any {
        return value
    }
}
