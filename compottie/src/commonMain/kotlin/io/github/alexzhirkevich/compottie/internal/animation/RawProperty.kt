package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState

internal interface RawProperty<out T : Any> {

    /**
     * "ix" value from the JSON scheme
     * */
    val index : Int?

    /**
     * Raw property interpolation without dynamic properties and expressions
     * */
    fun raw(state: AnimationState) : T
}

internal interface RawKeyframeProperty<out T : Any,out K : Keyframe<*>>  : RawProperty<T> {

    val keyframes: List<K>
}

internal interface AnimatedProperty<out T : Any> : RawProperty<T> {

    /**
     * Interpolation including dynamic properties and expressions.
     * Call from expressions will overflow the stack.
     * */
    fun interpolated(state: AnimationState) : T = raw(state)
}

internal interface AnimatedKeyframeProperty<out T : Any, out K : Keyframe<*>>
    : AnimatedProperty<T>, RawKeyframeProperty<T, K>