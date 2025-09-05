package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.keight.js.JsAny

internal fun interface VectorKeyframeMapper {
    fun FloatArrayVectorKeyframe.map(start : FloatArray, end : FloatArray, progress: Float) : Offset
}

internal class VectorKeyframeAnimation(
    override val index: Int?,
    sourceKeyframes: List<VectorKeyframe>,
    private val emptyValue : Offset,
    private val map : VectorKeyframeMapper
) : RawKeyframeProperty<Offset, FloatArrayVectorKeyframe> {

    override val keyframes = sourceKeyframes.map {
        FloatArrayVectorKeyframe(
            start = it.start?.toFloatArray(),
            end = it.end?.toFloatArray(),
            time = it.time,
            hold = it.hold,
            inValue = it.inValue,
            outValue = it.outValue,
            inTangent = it.inTangent?.toFloatArray(),
            outTangent = it.outTangent?.toFloatArray()
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
                    requireNotNull(
                        start,
                        InvalidKeyframeError
                    ),
                    requireNotNull(
                        endHold ?: sortedKeyframes.getOrNull(1)?.start,
                        InvalidKeyframeError
                    ),
                    0f,
                ).packedValue
            }
        }
    }

    private val targetValue : Long get()  {

        val preLast = sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)

        return sortedKeyframes.last().run {
            with(map) {
                map(
                    requireNotNull(
                        preLast?.start ?: start,
                        InvalidKeyframeError
                    ),
                    requireNotNull(
                        start ?: preLast?.end ?: preLast?.start,
                        InvalidKeyframeError
                    ),
                    1f,
                ).packedValue
            }
        }
    }

    private fun rawPacked(state: AnimationState): Long {
        return when {
            sortedKeyframes.isEmpty() -> emptyValue.packedValue
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
                            requireNotNull(
                                keyframe.start,
                                InvalidKeyframeError
                            ),
                            requireNotNull(
                                keyframe.endHold
                                    ?: sortedKeyframes.getOrNull(kfIdx + 1)?.start,
                                InvalidKeyframeError
                            ),
                            progress,
                        ).packedValue
                    }
                }
            }
        }
    }

    override fun raw(state: AnimationState): Offset {
        return Offset(rawPacked(state))
    }
}

internal class FloatArrayVectorKeyframe(
    override val start : FloatArray? = null,
    override val end : FloatArray? = null,
    override val time : Float,
    override val hold: Boolean = false,
    override val endHold: FloatArray? = if (hold) start else end,
    override val inValue : BezierInterpolation? = null,
    override val outValue : BezierInterpolation? = null,
    val inTangent: FloatArray? = null,
    val outTangent: FloatArray? = null,
): Keyframe<FloatArray> by BaseKeyframe(
    start = start,
    end = end,
    time = time,
    hold = hold,
    inValue = inValue,
    outValue = outValue
) {

    fun copy(): FloatArrayVectorKeyframe {
        return FloatArrayVectorKeyframe(
            start = start,
            end = end,
            time = time,
            hold = hold,
            inValue = inValue,
            outValue = outValue,
            inTangent = inTangent,
            outTangent = outTangent
        )
    }
}


private val InvalidKeyframeError = {
    "Invalid keyframe"
}
