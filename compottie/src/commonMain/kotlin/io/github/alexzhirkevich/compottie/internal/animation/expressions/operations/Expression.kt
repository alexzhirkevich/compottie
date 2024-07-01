package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal class Expression(
    val variables : Map<String, Any>,
    val expression : String
) {

    operator fun invoke(state: AnimationState) {

    }
}

