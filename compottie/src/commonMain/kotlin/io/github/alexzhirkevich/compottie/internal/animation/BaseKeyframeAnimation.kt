package io.github.alexzhirkevich.compottie.internal.animation

import kotlin.math.roundToInt


internal class BaseKeyframeAnimation<T,K>(
    keyframes: List<Keyframe<K>>,
    private val emptyValue : T,
    private val map : Keyframe<K>.(start : K, end : K, progress: Float,  frame: Float) -> T
) : KeyframeAnimation<T> {

    private val sortedKeyframes = keyframes
        .sortedBy { it.time }
        .takeIf { it != keyframes }
        ?: keyframes // ensure keyframes are sorted. don't store extra refs list if so

    private val timeIntervals = (0..<sortedKeyframes.lastIndex).map {
        sortedKeyframes[it].time..sortedKeyframes[it + 1].time
    }

    private val firstFrame: Float by lazy {
        sortedKeyframes.first().time
    }

    private val lastFrame: Float by lazy {
        sortedKeyframes.last().time
    }

    private val initialValue by lazy {
        keyframes.first().run {
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
                firstFrame
            )
        }
    }

    private val targetValue by lazy {
        keyframes.last().run {
            map(
                requireNotNull(
                    sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)?.start,
                    InvalidKeyframeError
                ),
                requireNotNull(
                    sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)?.endHold ?: start,
                    InvalidKeyframeError
                ),
                1f,
                lastFrame
            )
        }
    }

    override fun interpolated(frame: Float): T {
        return when {
            sortedKeyframes.isEmpty() -> emptyValue
            frame >= lastFrame -> targetValue
            frame <= firstFrame -> initialValue
            else -> {

                val kfIdx = timeIntervals.binarySearch {
                    when {
                        frame < it.start -> 1
                        frame > it.endInclusive -> -1
                        else -> 0
                    }
                }

                require(kfIdx >= 0, InvalidKeyframeError)

                val progress = timeIntervals[kfIdx].let {
                    (frame - it.start) / (it.endInclusive - it.start)
                }

                sortedKeyframes[kfIdx].run {
                    map(
                        requireNotNull(
                            sortedKeyframes[kfIdx].start,
                            InvalidKeyframeError
                        ),
                        requireNotNull(
                            sortedKeyframes[kfIdx].endHold
                                ?: sortedKeyframes.getOrNull(kfIdx + 1)?.start,
                            InvalidKeyframeError
                        ),
                        progress,
                        frame
                    )
                }
            }
        }
    }
}

private val InvalidKeyframeError = {
    "Invalid keyframe"
}

