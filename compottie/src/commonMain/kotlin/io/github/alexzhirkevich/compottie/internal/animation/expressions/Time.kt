package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.Expression
import io.github.alexzhirkevich.keight.js.FunctionParam
import io.github.alexzhirkevich.keight.js.JSFunction
import io.github.alexzhirkevich.keight.js.js
import kotlin.math.abs
import kotlin.math.max

internal fun JsFramesToTime() = JSFunction(
    FunctionParam("frame", default = Expression {  it.state.frame.js() }),
    FunctionParam("fps", default = Expression { (1f / it.state.composition.frameRate).js() }),
) {
    val frame = (it[0]?.toKotlin(this) as Number).toFloat()
    val fps = (it[1]?.toKotlin(this) as Number).toFloat()
    (frame / fps).js()
}

internal fun JsTimeToFrames() = JSFunction(
    FunctionParam("time", default =  Expression {
        (it.state.time.inWholeMilliseconds / 100f + it.state.thisComp.startTime).js()
    }),
    FunctionParam("fps", default = Expression {  (1f / it.state.composition.frameRate).js() }),
    FunctionParam("isDuration", default = Expression { false.js() })
){
    val time = (it[0]?.toKotlin(this) as Number).toFloat()
    val fps = (it[1]?.toKotlin(this) as Number).toFloat()
    val isDuration = (it[2]?.toKotlin(this) as Boolean)

    if (isDuration) {
        (state.absoluteTime.inWholeMilliseconds / 100f + time) * fps
    } else {
        time * fps
    }.js()
}

internal fun JsLoopOut(
    isDuration : Boolean
) = Callable {

    val prop = state.thisProperty!!

    if (prop !is RawKeyframeProperty<*, *>) {
        return@Callable prop.raw(state).toJs()
    }

    val type = it.getOrNull(0)?.toKotlin(this)?.toString()
    var duration = (it.getOrNull(1)?.toKotlin(this) as? Number)?.toInt()

    val lastKeyFrame = prop.keyframes.last().time

    if (state.frame <= lastKeyFrame) {
        return@Callable prop.raw(state).toJs()
    }
    val cycleDuration: Float
    val firstKeyFrame: Float

    if (isDuration) {
        cycleDuration = if (duration == null) {
            max(0f,  lastKeyFrame - (state.thisLayer.inPoint ?: 0f))
        } else {
            abs(lastKeyFrame - state.composition.frameRate * duration)
        }
        firstKeyFrame = lastKeyFrame - cycleDuration
    } else {
        if (duration == null || duration > prop.keyframes.lastIndex) {
            duration = prop.keyframes.lastIndex
        }
        firstKeyFrame = prop.keyframes[prop.keyframes.lastIndex - duration].time
        cycleDuration = lastKeyFrame - firstKeyFrame
    }

    when (type?.lowercase()) {
        "pingpong" -> {
            val iterations = ((state.frame - firstKeyFrame) / cycleDuration).toInt()
            if (iterations % 2 == 1) {
                return@Callable state.onFrame(
                    cycleDuration - (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
                    prop::raw
                ).toJs()
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

            return@Callable sum(
                mul(
                    sub(endV.toJs(), initV.toJs()),
                    repeats.js()
                ),
                current.toJs()
            )
        }

        "continue" -> {
            val lastValue = state.onFrame(lastKeyFrame, prop::raw)
            val nextLastValue = state.onFrame(lastKeyFrame - 0.001f, prop::raw)

            val lastJs = lastValue.toJs()

            return@Callable mul(
                sum(
                    lastJs,
                    sub(lastJs, nextLastValue.toJs())
                ),
                div(
                    sub(state.frame.js(), lastKeyFrame.js()),
                    0.001.js()
                )
            )
        }
    }

    state.onFrame(
        (state.frame - firstKeyFrame) % cycleDuration + firstKeyFrame,
        prop::raw
    ).toJs()
}

internal fun JSLoopIn(
    isDuration : Boolean
) = Callable {

    val prop = state.thisProperty!!

    if (prop !is RawKeyframeProperty<*, *>) {
        return@Callable prop.raw(state).toJs()
    }

    val type = it.getOrNull(0)?.toKotlin(this)?.toString()
    var duration = (it.getOrNull(1)?.toKotlin(this )as? Number)?.toInt() ?: 0

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
    when (type?.lowercase()) {
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

            return@Callable sub(
                current.toJs(),
                mul(
                    sub(endV.toJs(), initV.toJs()),
                    repeats.js()
                )
            )
        }

        "continue" -> {
            val firstValue = state.onFrame(firstKeyframe, prop::raw)
            val nextFirstValue = state.onFrame(firstKeyframe + 0.001f, prop::raw)
            val firstJs = firstValue.toJs()
            return@Callable sum(
                firstJs, mul(
                    mul(
                        sub(firstJs, nextFirstValue.toJs()),
                        sub(firstJs, state.frame.js())
                    ),
                    1000.js()
                )
            )
        }
    }
    state.onFrame(
        cycleDuration - (firstKeyframe - state.frame) % cycleDuration + firstKeyframe,
        prop::raw
    ).toJs()
}
