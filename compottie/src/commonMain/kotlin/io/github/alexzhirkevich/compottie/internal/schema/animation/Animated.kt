package io.github.alexzhirkevich.compottie.internal.schema.animation

interface Animated<T> {

    fun interpolated(frame : Int) : T
}