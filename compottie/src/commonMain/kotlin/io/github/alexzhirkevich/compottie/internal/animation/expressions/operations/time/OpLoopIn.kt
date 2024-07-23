package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.minus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.plus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.times
import kotlin.math.abs
import kotlin.math.max

internal fun OpLoopIn(
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

    val firstKeyframe = prop.keyframes.first().time

    if (state.frame >= firstKeyframe) {
        return@Expression prop.raw(state)
    }
    val cycleDuration: Float
    val lastKeyFrame: Float

    if (isDuration) {
        cycleDuration = if (duration == 0) {
            max(0f, (state.layer.outPoint ?: 0f) - firstKeyframe)
        } else {
            abs(state.composition.frameRate * duration)
        }
        lastKeyFrame = firstKeyframe + cycleDuration
    } else {
        if (duration == 0 || duration > prop.keyframes.lastIndex) {
            duration = prop.keyframes.lastIndex
        }
        lastKeyFrame = prop.keyframes[duration].time
        cycleDuration = lastKeyFrame - firstKeyframe
    }

    when (type.lowercase()) {
        "pingpong" -> {
            val iterations = ((firstKeyframe - state.frame) / cycleDuration).toInt()
            if (iterations % 2 == 0) {
                return@Expression state.onFrame(
                    (firstKeyframe - state.frame) % cycleDuration + firstKeyframe,
                    prop::raw
                )
            }
        }

        "offset" -> {
            val initV = state.onFrame(firstKeyframe, prop::raw)
            val endV = state.onFrame(lastKeyFrame, prop::raw)
            val current = state.onFrame(
                (cycleDuration - ((firstKeyframe - state.frame) % cycleDuration) + firstKeyframe),
                prop::raw
            )

            val repeats = ((firstKeyframe - state.frame) / cycleDuration).toInt() + 1

            return@Expression current - (endV - initV) * repeats;
        }

        "continue" -> {
            val firstValue = state.onFrame(firstKeyframe, prop::raw)
            val nextFirstValue = state.onFrame(firstKeyframe + 0.001f, prop::raw)
            return@Expression firstValue + ((firstValue - nextFirstValue) * (firstValue - state.frame)) * 1000
        }
    }
    state.onFrame(
        cycleDuration - (firstKeyframe - state.frame) % cycleDuration + firstKeyframe,
        prop::raw
    )
}
