package io.github.alexzhirkevich.compottie;

/**
 * Use [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see animateLottieCompositionAsState
 */
expect sealed class LottieClipSpec



expect object LottieClipSpecs {

    /**
     * Play the animation between these two progress values.
     */
    fun Progress(min: Float, max: Float) : LottieClipSpec

    /**
     * Play the animation between these two frames. [maxInclusive] determines whether the animation
     * should play the max frame or stop one frame before it.
     */
    fun Frame(
        min: Int? = null,
        max: Int? = null,
        maxInclusive: Boolean = true,
    ) : LottieClipSpec
}
