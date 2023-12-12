package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/**
 * [LottieAnimationState] is a value holder that contains information about the current Lottie animation.
 *
 * The primary values are [LottieAnimationState.progress] and [LottieAnimationState.composition]. These
 * value should be passed into the main [LottieAnimation] composable.
 *
 * @see progress
 * @see composition
 * @see animateLottieCompositionAsState
 */
@Stable
actual interface LottieAnimationState : State<Float> {
    actual val isPlaying: Boolean

    actual val progress: Float

    actual val iteration: Int

    actual val iterations: Int

    actual val reverseOnRepeat: Boolean

    val clipSpec: LottieClipSpec?

    actual val speed: Float

    actual val useCompositionFrameRate: Boolean

    actual val composition: LottieComposition?

    val lastFrameNanos: Long get() = AnimationConstants.UnspecifiedTime

    actual val isAtEnd: Boolean
}