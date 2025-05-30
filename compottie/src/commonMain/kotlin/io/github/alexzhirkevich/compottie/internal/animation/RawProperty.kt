package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JSLoopIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JSTemporalWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JSWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JsLoopOut
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JsSmooth
import io.github.alexzhirkevich.compottie.internal.animation.expressions.onTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.animation.expressions.toJs
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js

internal interface RawProperty<out T : Any> : JsAny {

    /**
     * "ix" value from the JSON scheme
     * */
    val index: Int?

    /**
     * Raw property interpolation without dynamic properties and expressions
     * */
    fun raw(state: AnimationState): T

    val cache: MutableMap<String, Any?>

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = listOf(
        "value".js(),
        "propertyIndex".js(),
        "loopIn".js(),
        "loopInDuration".js(),
        "loopOut".js(),
        "loopOutDuration".js(),
        "wiggle".js(),
        "temporalWiggle".js(),
        "smooth".js(),
    )

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property.toString()) {
            "value" -> raw(runtime.state).toJs()
            "valueAtTime" -> cache.getOrPut("valueAtTime") {
                Callable { onTime(it.getOrNull(0)) { raw(it).toJs() } }
            } as JsAny?
            "propertyIndex" -> index?.js() ?: Undefined
            "loopIn" -> cache.getOrPut("loopIn") { JSLoopIn(false) } as JsAny?
            "loopInDuration" -> cache.getOrPut("loopInDuration") { JSLoopIn(true) } as JsAny?
            "loopOut" -> cache.getOrPut("loopOut") { JsLoopOut(false) } as JsAny?
            "loopOutDuration" -> cache.getOrPut("loopOutDuration") { JsLoopOut(true) } as JsAny?
            "wiggle" -> cache.getOrPut("wiggle") { JSWiggle(this) } as JsAny?
            "temporalWiggle" -> cache.getOrPut("temporalWiggle") { JSTemporalWiggle(this) } as JsAny?
            "smooth" -> cache.getOrPut("smooth") { JsSmooth(this) } as JsAny?
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