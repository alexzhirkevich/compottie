package io.github.alexzhirkevich.compottie.internal.schema.animation

import kotlin.math.roundToInt


internal class BaseKeyframeAnimation<T,K>(
    keyframes: List<Keyframe<K>>,
    private val emptyValue : T,
    private val map : Keyframe<K>.(K, K, Float, Int) -> T
) : KeyframeAnimation<T> {

    private val sortedKeyframes = keyframes.sortedBy { it.time }

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
                    sortedKeyframes[0].start,
                    InvalidKeyframeError
                ),
                requireNotNull(
                    sortedKeyframes[0].end ?: sortedKeyframes.getOrNull(1)?.start,
                    InvalidKeyframeError
                ),
                0f,
                firstFrame.roundToInt()
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
                    sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)?.end
                        ?: sortedKeyframes.last().start,
                    InvalidKeyframeError
                ),
                1f,
                lastFrame.roundToInt()
            )
        }
    }

    override fun interpolated(frame: Int): T {
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
                    if (frame.toFloat() in s..<e) {
                        kfIdx = i
                        progress = (frame - s) / (e - s).toFloat()
                        break
                    }
                }

                sortedKeyframes[kfIdx].run {
                    map(
                        requireNotNull(
                            sortedKeyframes[kfIdx].start,
                            InvalidKeyframeError
                        ),
                        requireNotNull(
                            sortedKeyframes[kfIdx].end
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

