package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal interface PropertyAnimation<out T : Any> {


    val index : Int?
    /**
     * Raw property interpolation without dynamic properties and expressions
     * */
    fun rawInterpolated(state: AnimationState) : T = interpolated(state)

    /**
     * Interpolation including dynamic properties and expressions.
     * Call from expressions will overflow the stack.
     * */
    fun interpolated(state: AnimationState) : T
}

internal interface KeyframeAnimation<T : Any, out K : Keyframe<*>> : PropertyAnimation<T> {
    val keyframes : List<K>
}