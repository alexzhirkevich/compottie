package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.JSLoopIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.animation.expressions.toJs
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js

internal interface RawProperty<out T : Any> : JsAny {

    /**
     * "ix" value from the JSON scheme
     * */
    val index : Int?

    /**
     * Raw property interpolation without dynamic properties and expressions
     * */
    fun raw(state: AnimationState) : T

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = listOf(
        "value".js(),
        "propertyIndex".js()
    )

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when(property.toString()){
            "value" -> raw(runtime.state).toJs()
            "propertyIndex" -> index?.js() ?: Undefined
            "loopIn" -> JSLoopIn(false)
            "loopInDuration" -> JSLoopIn(true)
            else -> super.get(property, runtime)
        }
    }
}

internal interface RawKeyframeProperty<out T : Any,out K : Keyframe<*>>  : RawProperty<T> {

    val keyframes: List<K>

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = super.keys(runtime, excludeSymbols, excludeNonEnumerables) +
            "numKeys".js()

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()) {
            "numKeys" -> keyframes.size.js()
            else -> super.get(property, runtime)
        }
    }
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