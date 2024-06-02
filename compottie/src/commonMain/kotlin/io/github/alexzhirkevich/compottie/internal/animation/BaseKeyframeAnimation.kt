package io.github.alexzhirkevich.compottie.internal.animation

import kotlin.math.roundToInt


internal class BaseKeyframeAnimation<T,K>(
    keyframes: List<Keyframe<K>>,
    private val emptyValue : T,
    private val map : Keyframe<K>.(start : K, end : K, progress: Float,  frame: Float) -> T
) : KeyframeAnimation<T> {

    private val sortedKeyframes = keyframes//.sortedBy { it.time }

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
                var kfIdx = sortedKeyframes.lastIndex
                var progress = 1f

                for (i in 0..<sortedKeyframes.lastIndex) {
                    val s = sortedKeyframes[i].time
                    val e = sortedKeyframes[i + 1].time
                    if (frame in s..e) {
                        kfIdx = i
                        progress = (frame - s) / (e - s)
                        break
                    }
                }

//                println(kfIdx)

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

