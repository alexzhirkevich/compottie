package io.github.alexzhirkevich.compottie.internal.schema.properties

interface Animated<T> {

    fun interpolated(frame : Int) : T
}