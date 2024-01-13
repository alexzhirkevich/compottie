package io.github.alexzhirkevich.compottie

expect class Marker {
    val startFrame : Float
    val durationFrames : Float
}


/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieCompositionAsState
 */
expect sealed class LottieClipSpec {

    internal abstract fun getMinProgress(composition: LottieComposition): Float

    internal abstract fun getMaxProgress(composition: LottieComposition): Float

    /**
     * Play the animation between these two frames. [maxInclusive] determines whether the animation
     * should play the max frame or stop one frame before it.
     */
    class Frame(
        min: Int? = null,
        max: Int? = null,
        maxInclusive: Boolean = true,
    ) : LottieClipSpec

    /**
     * Play the animation between these two progress values.
     */
    class Progress(
        min: Float = 0f,
        max: Float = 1f,
    ) : LottieClipSpec

    /**
     * Play the animation from minMarker until maxMarker. If maxMarker represents the end of your animation,
     * set [maxInclusive] to true. If the marker represents the beginning of the next section, set
     * it to false to stop the animation at the frame before maxMarker.
     */
    class Markers(
        min: String? = null,
        max: String? = null,
        maxInclusive: Boolean = true
    ) : LottieClipSpec

    /**
     * Play the animation from the beginning of the marker for the duration of the marker itself.
     * The duration can be set in After Effects.
     */
    class Marker(marker: String) : LottieClipSpec
}
