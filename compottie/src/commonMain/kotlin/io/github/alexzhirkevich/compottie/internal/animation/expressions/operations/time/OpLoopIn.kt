package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.minus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.plus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.times
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.animation.expressions.toJs
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.js.js
import kotlin.math.abs
import kotlin.math.max

internal fun JSLoopIn(
    isDuration : Boolean
) = Callable {

    val prop = get("thisProp".js()) as RawProperty<*>

    if (prop !is RawKeyframeProperty<*, *>) {
        return@Callable prop.raw(state).toJs()
    }

    val type = it[0]?.toKotlin(this)?.toString() ?: "cycle"
    var duration = (it[1]?.toKotlin(this )as? Number)?.toInt() ?: 0

    val firstKeyframe = prop.keyframes.first().time

    if (state.frame >= firstKeyframe) {
        return@Callable prop.raw(state).toJs()
    }

    val cycleDuration: Float
    val lastKeyFrame: Float

    if (isDuration) {
        cycleDuration = if (duration == 0) {
            max(0f, (state.thisLayer.outPoint ?: 0f) - firstKeyframe)
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
                return@Callable state.onFrame(
                    (firstKeyframe - state.frame) % cycleDuration + firstKeyframe,
                    prop::raw
                ).toJs()
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

            return@Callable (current - (endV - initV) * repeats).toJs()
        }

        "continue" -> {
            val firstValue = state.onFrame(firstKeyframe, prop::raw)
            val nextFirstValue = state.onFrame(firstKeyframe + 0.001f, prop::raw)
            return@Callable (firstValue + ((firstValue - nextFirstValue) * (firstValue - state.frame)) * 1000).toJs()
        }
    }
    state.onFrame(
        cycleDuration - (firstKeyframe - state.frame) % cycleDuration + firstKeyframe,
        prop::raw
    ).toJs()
}
