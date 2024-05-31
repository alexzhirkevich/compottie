package io.github.alexzhirkevich.compottie.internal.animation

interface KeyframeAnimation<T> {

    fun interpolated(frame: Float) : T
}