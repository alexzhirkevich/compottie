package io.github.alexzhirkevich.compottie.internal.schema.animation

interface KeyframeAnimation<T> {

    fun interpolated(frame: Float) : T
}