package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


/**
 * Returns a [LottieAnimationState] representing the progress of an animation.
 *
 * @param composition The composition to render. This should be retrieved with [rememberLottieComposition].
 * @param isPlaying Whether or not the animation is currently playing. Note that the internal
 *                  animation may end due to reaching the target iterations count. If that happens,
 *                  the animation may stop even if this is still true. You can observe the returned
 *                  [LottieAnimationState.isPlaying] to determine whether the underlying animation
 *                  is still playing.
 * @param restartOnPlay If isPlaying switches from false to true, restartOnPlay determines whether
 *                      the progress and iteration gets reset.
 * @param reverseOnRepeat Defines what this animation should do when it reaches the end. This setting
 *                        is applied only when [iterations] is either greater than 0 or [LottieConstants.IterateForever].
 *                        Defaults to `false`.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 *              Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param iterations The number of times the animation should repeat before stopping. It must be
 *                    a positive number. [LottieConstants.IterateForever] can be used to repeat forever.
 * @param cancellationBehavior The behavior that this animation should have when cancelled. In most cases,
 *                             you will want it to cancel immediately. However, if you have a state based
 *                             transition and you want an animation to finish playing before moving on to
 *                             the next one then you may want to set this to [LottieCancellationBehavior.OnIterationFinish].
 * @param ignoreSystemAnimatorScale By default, Lottie will respect the system animator scale set in developer options or set to 0
 *                                  by things like battery saver mode. When set to 0, the speed will effectively become [Integer.MAX_VALUE].
 *                                  Set this to false if you want to ignore the system animator scale and always default to normal speed.
 */
@Composable
expect fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    speed: Float = 1f,
    iterations: Int = 1,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    ignoreSystemAnimatorScale: Boolean = false,
    useCompositionFrameRate: Boolean = false,
) : LottieAnimationState


/**
 * This is the base LottieAnimation composable. It takes a composition and renders it at a specific progress.
 *
 * The overloaded version of [LottieAnimation] that handles playback and is sufficient for most use cases.
 *
 * @param composition The composition that will be rendered. To generate a [LottieComposition], you can use
 *                    [rememberLottieComposition].
 * @param progress A provider for the progress (between 0 and 1) that should be rendered. If you want to render a
 *                         specific frame, you can use [LottieComposition.getFrameForProgress]. In most cases, you will want
 *                         to use one of the overloaded LottieAnimation composables that drives the animation for you.
 *                         The overloads that have isPlaying as a parameter instead of progress will drive the
 *                         animation automatically. You may want to use this version if you want to drive the animation
 *                         from your own Animatable or via events such as download progress or a gesture.
 *                     For more details, refer to the docs of [AsyncUpdates].
 */
@Composable
expect fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier = Modifier
)

/**
 * This is like [LottieAnimation] except that it handles driving the animation via [animateLottieCompositionAsState]
 * instead of taking a progress provider.
 *
 * @see LottieAnimation
 * @see animateLottieCompositionAsState
 */
@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 1,
    reverseOnRepeat: Boolean = false,
) {
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = restartOnPlay,
        reverseOnRepeat = reverseOnRepeat,
        speed = speed,
        iterations = iterations,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}


