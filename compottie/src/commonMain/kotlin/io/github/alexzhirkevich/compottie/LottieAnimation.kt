package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

expect enum class LottieCancellationBehavior {
    /**
     * Stop animation immediately and return early.
     */
    Immediately,

    /**
     * Delay cancellations until the current iteration has fully completed.
     * This can be useful in state based transitions where you want one animation to finish its
     * animation before continuing to the next.
     */
    OnIterationFinish,
}

expect class LottieComposition

expect val LottieComposition.durationMillis : Int

/**
 * Create and remember lottie composition.
 *
 * @param data Lottie JSON string
 * */
@Composable
expect fun rememberLottieComposition(data : String) : LottieComposition?

/**
 * State of the lottie animation progress
 *
 * @param composition animation composition. Use [rememberLottieComposition]
 * @param repeatMode animation repeat mode
 * @param isPlaying is animation progress is running.
 * Animation starts to play from start when state changes from true to false.
 * And seeks to the first frame and stops when true changed to false.
 * @param iterations animation iteration count. Use [Int.MAX_VALUE] for infinite animation
 *
 * @see rememberLottieComposition
 * */
@Composable
expect fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    repeatMode: RepeatMode = RepeatMode.Restart,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    isPlaying : Boolean = true,
    iterations : Int = Int.MAX_VALUE,
) : State<Float>

/**
 * Lottie animation.
 *
 * @param composition animation composition. Use [rememberLottieComposition]
 * @param progress animation progress. Use [animateLottieCompositionAsState]
 *
 * @see rememberLottieComposition
 * @see animateLottieCompositionAsState
 * */
@Composable
expect fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier = Modifier
)
