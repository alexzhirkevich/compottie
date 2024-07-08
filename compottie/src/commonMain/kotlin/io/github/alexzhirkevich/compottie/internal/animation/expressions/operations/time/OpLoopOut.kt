package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.minus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.plus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.times
import kotlin.math.abs
import kotlin.math.max

internal class OpLoopOut(
    private val property: Expression,
    private val name : Expression?,
    private val numKf : Expression?,
    private val isDuration : Boolean
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        val prop = this.property(property, context, state) as RawProperty<Any>

        if (prop !is RawKeyframeProperty<*, *>) {
            return prop.raw(state)
        }

        val type = (name?.invoke(property, context, state) ?: "cycle") as String
        var duration = ((numKf?.invoke(property, context, state) ?: 0) as Number).toInt()

        val lastKeyFrame = prop.keyframes.last().time

        if (state.frame <= lastKeyFrame) {
            return prop.raw(state)
        }
        val durationFlag = false //TODO: ???
        val cycleDuration: Float
        val firstKeyFrame: Float

        if (isDuration) {
            cycleDuration = if (duration == 0) {
                max(0f,  lastKeyFrame - (state.layer.inPoint ?: 0f))
            } else {
                abs(lastKeyFrame - state.composition.frameRate * duration)
            }
            firstKeyFrame = lastKeyFrame - cycleDuration
        } else {
            if (duration == 0 || duration > prop.keyframes.lastIndex) {
                duration = prop.keyframes.lastIndex
            }
            firstKeyFrame = prop.keyframes[prop.keyframes.lastIndex - duration].time
            cycleDuration = lastKeyFrame - firstKeyFrame
        }

        when (type.lowercase()) {
            "pingpong" -> {
                val iterations = ((state.frame - firstKeyFrame) / cycleDuration).toInt()
                if (iterations % 2 == 1) {
                    return state.onFrame(
                        cycleDuration - (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
                        prop::raw
                    )
                }
            }

            "offset" -> {
                val initV = state.onFrame(firstKeyFrame, prop::raw)
                val endV = state.onFrame(lastKeyFrame, prop::raw)
                val current = state.onFrame(
                    (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
                    prop::raw
                )

                val repeats = ((state.frame - firstKeyFrame) / cycleDuration).toInt()

                return (endV - initV) * repeats + current
            }

            "continue" -> {
                val lastValue = state.onFrame(lastKeyFrame, prop::raw)
                val nextLastValue = state.onFrame(lastKeyFrame - 0.001f, prop::raw)
                return  lastValue + (lastValue - nextLastValue) * (((state.frame - lastKeyFrame)) / 0.001)
            }
        }

        return state.onFrame(
            (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
            prop::raw
        )
    }
}
