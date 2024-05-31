package io.github.alexzhirkevich.compottie.internal.schema.animation


internal class KeyframeAnimation<T,K>(
    keyframes: List<Keyframe<K>>,
    private val emptyValue : T,
    val map : (start : K, end : K, Float) -> T,
) : Animated<T> {

    private val sortedKeyframes = keyframes.sortedBy { it.time }

    private val firstFrame: Int by lazy {
        sortedKeyframes.first().time
    }

    private val lastFrame: Int by lazy {
        sortedKeyframes.last().time
    }

    private val initialValue by lazy {
        map(
            requireNotNull(
                sortedKeyframes[0].start,
                InvalidKeyframeError
            ),
            requireNotNull(
                sortedKeyframes[0].end ?: sortedKeyframes.getOrNull(1)?.start,
                InvalidKeyframeError
            ),
            0f
        )
    }

    private val targetValue by lazy {
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
            1f
        )
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
                    if (frame in s..<e) {
                        kfIdx = i
                        progress = (frame - s) / (e - s).toFloat()
                        break
                    }
                }

                map(
                    requireNotNull(
                        sortedKeyframes[kfIdx].start,
                        InvalidKeyframeError
                    ),
                    requireNotNull(
                        sortedKeyframes[kfIdx].end ?: sortedKeyframes.getOrNull(kfIdx + 1)?.start,
                        InvalidKeyframeError
                    ),
                    sortedKeyframes[kfIdx].easing.transform(progress)
                )
            }
        }
    }
}

private val InvalidKeyframeError = {
    "Invalid keyframe"
}

