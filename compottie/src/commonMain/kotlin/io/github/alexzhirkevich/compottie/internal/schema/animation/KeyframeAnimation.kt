package io.github.alexzhirkevich.compottie.internal.schema.animation

interface KeyframeAnimation<T> {

    fun interpolated(frame : Int) : T
}