package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime

internal fun OpSmooth(
    prop : Expression,
    width : Expression? = null,
    samples : Expression? = null,
    time : Expression? = null
) = Expression { property, context, state ->
    val prop = prop(property, context, state) as RawProperty<Any>
    val width = (width?.invoke(property, context, state) as Number?)?.toFloat() ?: .4f
    val samples = (samples?.invoke(property, context, state) as Number?)?.toInt() ?: 5
    val time = (time?.invoke(property, context, state) as Number?)?.toFloat()

    if (time == null) {
        smooth(prop, state, width ?: .4f, samples ?: 5)
    } else {
        state.onTime(time) {
            smooth(prop, it, width ?: .4f, samples ?: 5)
        }
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

    val currentTime = OpGetTime.invoke(state)
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