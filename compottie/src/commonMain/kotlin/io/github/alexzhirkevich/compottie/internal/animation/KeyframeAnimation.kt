package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal interface KeyframeAnimation<T> {
    fun interpolated(state: AnimationState) : T
}