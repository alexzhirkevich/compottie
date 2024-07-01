package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal fun interface Operation {

    operator fun invoke(value : Any, state: AnimationState) : Any
}