package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime

internal class OpSmooth(
    private val prop : Expression,
    private val width : Expression? = null,
    private val samples : Expression? = null,
    private val time : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val prop = prop(property, context, state) as RawProperty<Any>
        val width = (width?.invoke(property, context, state) as Number?)?.toFloat() ?: .4f
        val samples = (samples?.invoke(property, context, state) as Number?)?.toInt() ?: 5
        val time = (time?.invoke(property, context, state) as Number?)?.toFloat()

        return invoke(prop, time, state, width, samples)
    }

    companion object {

        fun invoke(
            prop : RawProperty<Any>,
            time : Float?,
            state: AnimationState,
            width : Float?,
            samples : Int?
        ) : Any {
            return if (time == null) {
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
            var width = width

            if (prop !is RawKeyframeProperty<*, *> || samples <= 1) {
                return prop.raw(state)
            }

            width /= 2f

            val currentTime = OpGetTime.invoke(state)
            val initFrame = currentTime - width
            val endFrame = currentTime + width
            val sampleFrequency = endFrame-initFrame

            var value : Any? = null

            repeat (samples) { i ->
                val sampleValue = state.onTime(initFrame + i * sampleFrequency, prop::raw)

                when {
                    value is Number? && sampleValue is Number -> {
                        value = if (value == null){
                            sampleValue
                        } else {
                            (value as Number).toFloat() + sampleValue.toFloat()
                        }
                    }
                    value is List<*>? && sampleValue is List<*> -> {
                        sampleValue as List<Number>?
                        value as MutableList<Number>?

                        if (value == null){
                            value = sampleValue.toMutableList()
                        } else {
                            for (i in (value as MutableList<Number>).indices) {
                                (value as MutableList<Number>)[i] = (value as MutableList<Number>)[i].toFloat() + sampleValue[i].toFloat()
                            }
                        }
                    }
                }
            }

            when (value) {
                is Number -> value = (value as Number).toFloat() / samples
                is MutableList<*> -> {
                    repeat((value as List<*>).lastIndex) {
                        (value as MutableList<Float>)[it] =
                            (value as MutableList<Float>)[it] / samples
                    }
                }
            }
            return value!!
        }
    }
}