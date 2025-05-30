package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal interface ExpressionHolder {

    fun prepareExpressions(state: AnimationState)
}