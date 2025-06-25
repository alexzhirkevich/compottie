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

    val index: Int?

    val jsCache: MutableMap<String, JsAny?>

    var group : PropertyGroup?

    fun raw(state: AnimationState): T

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = listOf(
        "value".js,
        "valueAtTime".js,
        "propertyIndex".js,
        "propertyGroup".js,
        "transform".js,
        "loopIn".js,
        "loopInDuration".js,
        "loopOut".js,
        "loopOutDuration".js,
        "wiggle".js,
        "temporalWiggle".js,
        "smooth".js,
    )

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()) {
            "value" -> raw(runtime.state).toJs()
            "valueAtTime" -> jsCache.getOrPut("valueAtTime") {
                Callable { onTime(it.getOrNull(0)) { raw(it).toJs() } }
            }
            "propertyIndex" -> index?.js ?: Undefined
            "propertyGroup" -> jsCache.getOrPut("propertyGroup") {
                Callable {
                    var n = toNumber(it.getOrNull(0) ?: return@Callable Undefined).toInt()
                    var g: PropertyGroup? = group
                    while (--n > 0) {
                        g = g?.group
                    }
                    g
                }
            }
            "transform" -> group
            "loopIn" -> jsCache.getOrPut("loopIn") { JSLoopIn(false) }
            "loopInDuration" -> jsCache.getOrPut("loopInDuration") { JSLoopIn(true) }
            "loopOut" -> jsCache.getOrPut("loopOut") { JsLoopOut(false) } 
            "loopOutDuration" -> jsCache.getOrPut("loopOutDuration") { JsLoopOut(true) } 
            "wiggle" -> jsCache.getOrPut("wiggle") { JSWiggle(this) } 
            "temporalWiggle" -> jsCache.getOrPut("temporalWiggle") { JSTemporalWiggle(this) } 
            "smooth" -> jsCache.getOrPut("smooth") { JsSmooth(this) } 
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
            "numKeys".js

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()) {
            "numKeys" -> keyframes.size.js
            else -> super.get(property, runtime)
        }
    }
}

internal interface AnimatedProperty<out T : Any> : RawProperty<T> {

    /**
     * Property value interpolation including dynamics and expressions.
     * Should be called from the DrawScope. Calling from expressions can overflow the stack.
     * */
    fun interpolated(state: AnimationState) : T = raw(state)
}

internal interface AnimatedKeyframeProperty<out T : Any, out K : Keyframe<*>>
    : AnimatedProperty<T>, RawKeyframeProperty<T, K>