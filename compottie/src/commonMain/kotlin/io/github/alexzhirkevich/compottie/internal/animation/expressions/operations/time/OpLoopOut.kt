package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.minus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.plus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.times
import kotlin.math.abs
import kotlin.math.max

internal fun OpLoopOut(
    property: Expression,
    name : Expression?,
    numKf : Expression?,
    isDuration : Boolean
) = Expression { thisProperty, context, state ->
    val prop = property(thisProperty, context, state) as RawProperty<Any>

    if (prop !is RawKeyframeProperty<*, *>) {
        return@Expression prop.raw(state)
    }

    val type = (name?.invoke(thisProperty, context, state) ?: "cycle") as String
    var duration = ((numKf?.invoke(thisProperty, context, state) ?: 0) as Number).toInt()

    val lastKeyFrame = prop.keyframes.last().time

    if (state.frame <= lastKeyFrame) {
        return@Expression prop.raw(state)
    }
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
                return@Expression state.onFrame(
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

            return@Expression (endV - initV) * repeats + current
        }

        "continue" -> {
            val lastValue = state.onFrame(lastKeyFrame, prop::raw)
            val nextLastValue = state.onFrame(lastKeyFrame - 0.001f, prop::raw)
            return@Expression  lastValue + (lastValue - nextLastValue) * (((state.frame - lastKeyFrame)) / 0.001)
        }
    }

    state.onFrame(
        (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
        prop::raw
    )
}