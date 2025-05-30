package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.timeSeconds
import io.github.alexzhirkevich.keight.Callable

internal fun JsSmooth(
    prop : RawProperty<*>,
) = Callable {
    val width = (it.getOrNull(0)?.toKotlin(this) as? Number)?.toFloat() ?: .4f
    val samples = (it.getOrNull(1)?.toKotlin(this) as? Number)?.toInt() ?: 5
    val time = it.getOrNull(2)

    onTime(time){
        smooth(prop, it, width, samples).toJs()
    }
}

@Suppress("unchecked_cast")
private fun smooth(
    prop : RawProperty<Any>,
    state: AnimationState,
    width : Float,
    samples : Int
) : Any {

    if (prop !is RawKeyframeProperty<*, *> || samples <= 1) {
        return prop.raw(state)
    }

    val width = width/2f

    val currentTime = state.timeSeconds
    val initTime = currentTime - width
    val endTime = currentTime + width
    val sampleFrequency = endTime-initTime

    var value : Any? = null

    repeat (samples) { i ->
        val sampleValue = state.onTime(initTime + i * sampleFrequency, prop::raw)

        when {
            value is Number? && sampleValue is Number -> {
                value = if (value == null){
                    sampleValue
                } else {
                    (value as Number).toFloat() + sampleValue.toFloat()
                }
            }
            value is List<*>? && sampleValue is List<*> -> {
                val v = value

                if (v == null){
                    value = sampleValue.toMutableList()
                } else {
                    v as MutableList<Number>
                    sampleValue as List<Number>

                    for (i in v.indices) {
                        v[i] = v[i].toFloat() + sampleValue[i].toFloat()
                    }
                }
            }
        }
    }

    when (val v = value) {
        is Number -> value = v.toFloat() / samples
        is MutableList<*> -> {
            v as MutableList<Float>
            repeat(v.lastIndex) {
                v[it] = v[it] / samples
            }
        }
    }
    return value!!
}