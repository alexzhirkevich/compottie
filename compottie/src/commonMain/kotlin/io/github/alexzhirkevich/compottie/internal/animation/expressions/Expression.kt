package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal fun <T> evaluate(
    expression : String,
    state: AnimationState,
    value : T
) : T {

    // TODO: support expressions
    return value
}

