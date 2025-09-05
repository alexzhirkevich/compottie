package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.keight.js.JsAny


internal fun interface ColorKeyframeMapper {
    fun ColorKeyframe.map(start : Color, end : Color, progress: Float) : Color
}

internal class ColorKeyframeAnimation(
    override val index: Int?,
    sourceKeyframes: List<VectorKeyframe>,
    private val emptyValue : Color,
    private val map : ColorKeyframeMapper
) : RawKeyframeProperty<Color, ColorKeyframe> {

    override val keyframes: List<ColorKeyframe> = sourceKeyframes.map {
        ColorKeyframe(
            startColor = it.start?.toColor() ?: Color.Transparent,
            endColor = it.end?.toColor() ?: Color.Transparent,
            time = it.time,
            hold = it.hold,
            inValue = it.inValue,
            outValue = it.outValue
        )
    }

    override val jsCache: MutableMap<String, JsAny?> = HashMap()

    override var group: PropertyGroup? = null

    private val sortedKeyframes = keyframes
        .sortedBy(Keyframe<*>::time)
        .takeIf { it != keyframes }
        ?: keyframes // ensure keyframes are sorted. don't store extra refs list if so

    private val timeIntervals = if (keyframes.isNotEmpty()) {
        List(sortedKeyframes.lastIndex) {
            FloatRange(sortedKeyframes[it].time, sortedKeyframes[it + 1].time)
        }
    } else {
        emptyList()
    }

    private val firstFrame: Float by lazy { sortedKeyframes.first().time }

    private val lastFrame: Float by lazy { sortedKeyframes.last().time }

    private val initialValue : Long get() {
        return sortedKeyframes.first().run {
            with(map) {
                map(
                    startColor,
                    endHoldColor,
                    0f,
                ).value.toLong()
            }
        }
    }

    private val targetValue : Long get()  {

        val preLast = sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)

        return sortedKeyframes.last().run {
            with(map) {
                map(
                    preLast?.startColor ?: startColor,
                    startColor,
                    1f,
                ).value.toLong()
            }
        }
    }

    private fun rawPacked(state: AnimationState): Long {
        return when {
            sortedKeyframes.isEmpty() -> emptyValue.value.toLong()
            state.frame >= lastFrame -> targetValue
            state.frame <= firstFrame -> initialValue
            else -> {

                val kfIdx = timeIntervals.binarySearch {
                    when {
                        state.frame < it.start -> 1
                        state.frame > it.endInclusive -> -1
                        else -> 0
                    }
                }

                require(kfIdx >= 0, InvalidKeyframeError)

                val progress = timeIntervals[kfIdx].let {
                    (state.frame - it.start) / (it.endInclusive - it.start)
                }

                val keyframe = sortedKeyframes[kfIdx]
                keyframe.run {
                    with(map) {
                        map(
                            keyframe.startColor,
                            keyframe.endHoldColor,
                            progress,
                        ).value.toLong()
                    }
                }
            }
        }
    }

    override fun raw(state: AnimationState): Color {
        return Color(rawPacked(state))
    }
}

internal class ColorKeyframe(
    val startColor : Color,
    val endColor : Color,
    override val time : Float,
    override val hold: Boolean = false,
    val endHoldColor: Color = if (hold) startColor else endColor,
    override val inValue : BezierInterpolation? = null,
    override val outValue : BezierInterpolation? = null,
) : Keyframe<Color> by BaseKeyframe(
    start = startColor,
    end = endColor,
    time = time,
    hold = hold,
    inValue = inValue,
    outValue = outValue
) {
    override val start = startColor
    override val end = endColor
    override val endHold = endHoldColor

    fun copy(): ColorKeyframe {
        return ColorKeyframe(
            startColor = startColor,
            endColor = endColor,
            time = time,
            hold = hold,
            inValue = inValue,
            outValue = outValue
        )
    }
}


private val InvalidKeyframeError = {
    "Invalid keyframe"
}
