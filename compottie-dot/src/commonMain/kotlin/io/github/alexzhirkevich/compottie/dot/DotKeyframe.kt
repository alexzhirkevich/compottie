@file: Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.animation.Keyframe

internal interface DotKeyframe<T> {
    val frame : Float
    val value : T
    val hold : Boolean

    fun toCoreKeyframe() : Keyframe<*>
}