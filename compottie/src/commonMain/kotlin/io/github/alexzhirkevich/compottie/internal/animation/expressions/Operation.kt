package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal fun interface Operation {

    operator fun invoke(
        value: Any,
        variables: Map<String, Any>,
        state: AnimationState
    ): Any

}