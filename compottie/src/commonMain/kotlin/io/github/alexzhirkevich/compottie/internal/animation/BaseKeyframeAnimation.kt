package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState


internal class BaseKeyframeAnimation<T : Any, K, out KF : Keyframe<K>>(
    override val index: Int?,
    override val keyframes: List<KF>,
    private val emptyValue : T,
    private val map : KF.(start : K, end : K, progress: Float) -> T
) : KeyframeAnimation<T, KF> {

    private val sortedKeyframes = keyframes
        .sortedBy(Keyframe<*>::time)
        .takeIf { it != keyframes }
        ?: keyframes // ensure keyframes are sorted. don't store extra refs list if so

    private val timeIntervals = if (keyframes.isNotEmpty()) {
        (0..<sortedKeyframes.lastIndex).map {
            sortedKeyframes[it].time..sortedKeyframes[it + 1].time
        }
    } else emptyList()

    private val firstFrame: Float by lazy { sortedKeyframes.first().time }

    private val lastFrame: Float by lazy { sortedKeyframes.last().time }

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
            )
        }
    }

    private val targetValue by lazy {

        val preLast = sortedKeyframes.getOrNull(sortedKeyframes.lastIndex - 1)

        keyframes.last().run {
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
            )
        }
    }


    override fun interpolated(state: AnimationState): T {

        if (sortedKeyframes.isEmpty())
            return emptyValue

        return when {
            sortedKeyframes.isEmpty() -> emptyValue
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
                    )
                }
            }
        }
    }
}

private val InvalidKeyframeError = {
    "Invalid keyframe"
}
