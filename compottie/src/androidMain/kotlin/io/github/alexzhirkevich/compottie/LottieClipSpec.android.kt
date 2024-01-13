package io.github.alexzhirkevich.compottie

import com.airbnb.lottie.compose.LottieClipSpec as PlatformLottieClipSpec

/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieCompositionAsState
 */
@Suppress("INVISIBLE_MEMBER")
actual sealed class LottieClipSpec(
    val delegate : PlatformLottieClipSpec
) {
    internal actual abstract fun getMinProgress(composition: LottieComposition): Float
    internal actual abstract fun getMaxProgress(composition: LottieComposition): Float

    /**
     * Play the animation between these two frames. [maxInclusive] determines whether the animation
     * should play the max frame or stop one frame before it.
     */
    actual class Frame actual constructor(
        min: Int?,
        max: Int?,
        maxInclusive: Boolean
    ) : LottieClipSpec(PlatformLottieClipSpec.Frame(min, max, maxInclusive)) {

        override fun getMinProgress(composition: LottieComposition): Float {
            return delegate.getMinProgress(composition)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return delegate.getMaxProgress(composition)
        }

        override fun equals(other: Any?): Boolean {
            return (other as? Frame)?.delegate == delegate
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }
    }

    /**
     * Play the animation between these two progress values.
     */
    actual class Progress actual constructor(min: Float, max: Float) :
        LottieClipSpec(PlatformLottieClipSpec.Progress(min, max)) {
        override fun getMinProgress(composition: LottieComposition): Float {
            return delegate.getMinProgress(composition)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return delegate.getMaxProgress(composition)
        }

        override fun equals(other: Any?): Boolean {
            return (other as? Progress)?.delegate == delegate
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }
    }

    /**
     * Play the animation from minMarker until maxMarker. If maxMarker represents the end of your animation,
     * set [maxInclusive] to true. If the marker represents the beginning of the next section, set
     * it to false to stop the animation at the frame before maxMarker.
     */
    actual class Markers actual constructor(
        min: String?,
        max: String?,
        maxInclusive: Boolean
    ) : LottieClipSpec(PlatformLottieClipSpec.Markers(min, max, maxInclusive)) {
        override fun getMinProgress(composition: LottieComposition): Float {
            return delegate.getMinProgress(composition)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return delegate.getMaxProgress(composition)
        }

        override fun equals(other: Any?): Boolean {
            return (other as? Markers)?.delegate == delegate
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }
    }

    /**
     * Play the animation from the beginning of the marker for the duration of the marker itself.
     * The duration can be set in After Effects.
     */
    actual class Marker actual constructor(marker: String) :
        LottieClipSpec(PlatformLottieClipSpec.Marker(marker)) {
        override fun getMinProgress(composition: LottieComposition): Float {
            return delegate.getMinProgress(composition)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return delegate.getMaxProgress(composition)
        }

        override fun equals(other: Any?): Boolean {
            return (other as? Marker)?.delegate == delegate
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }
    }
}

/**
 * Play the animation from the beginning of the marker for the duration of the marker itself.
 * The duration can be set in After Effects.
 */
@Suppress("INVISIBLE_MEMBER")
internal class DelegatedLottieClipSpec(delegate: com.airbnb.lottie.compose.LottieClipSpec) :
    LottieClipSpec(delegate) {
    override fun getMinProgress(composition: LottieComposition): Float {
        return delegate.getMinProgress(composition)
    }

    override fun getMaxProgress(composition: LottieComposition): Float {
        return delegate.getMaxProgress(composition)
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Marker)?.delegate == delegate
    }

    override fun toString(): String {
        return delegate.toString()
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }
}
