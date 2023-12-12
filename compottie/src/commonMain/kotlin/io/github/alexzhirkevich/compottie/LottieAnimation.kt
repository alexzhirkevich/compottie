package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

enum class CancellationBehavior {
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

/**
 * Holds animation data
 * */
expect class LottieComposition

///**
// * Animation duration in seconds
// * */
//expect val LottieComposition.duration : Float

/**
 * Create and remember lottie composition.
 *
 * @param data Lottie JSON string
 * */
@Composable
expect fun rememberLottieComposition(data : String) : LottieComposition

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
    composition: LottieComposition,
    repeatMode: RepeatMode = RepeatMode.Restart,
    cancellationBehavior: CancellationBehavior = CancellationBehavior.Immediately,
    isPlaying : Boolean = true,
    iterations : Int = Int.MAX_VALUE,
) : State<Float>

/**
 * Lottie animation with manual progress control.
 *
 * @param composition animation composition. Use [rememberLottieComposition]
 * @param progress animation progress from 0 to 1. Use [animateLottieCompositionAsState]
 * @param modifier animation container modifier
 * @see rememberLottieComposition
 * @see animateLottieCompositionAsState
 * */
@Composable
expect fun LottieAnimation(
    composition : LottieComposition,
    progress : () -> Float,
    modifier: Modifier = Modifier
)

/**
 * Lottie animation with automatic progress control.
 *
 * @param composition animation composition. Use [rememberLottieComposition]
 * @param repeatMode animation repeat mode
 * @param isPlaying is animation progress is running.
 * Animation starts to play from start when state changes from true to false.
 * And seeks to the first frame and stops when true changed to false.
 * @param iterations animation iteration count. Use [Int.MAX_VALUE] for infinite animation
 *
 * @see animateLottieCompositionAsState
 * */
@Composable
fun LottieAnimation(
    composition: LottieComposition,
    modifier: Modifier = Modifier,
    repeatMode: RepeatMode = RepeatMode.Restart,
    cancellationBehavior: CancellationBehavior = CancellationBehavior.Immediately,
    isPlaying : Boolean = true,
    iterations : Int = Int.MAX_VALUE,
) {
    val progress = animateLottieCompositionAsState(
        composition = composition,
        repeatMode = repeatMode,
        cancellationBehavior = cancellationBehavior,
        isPlaying = isPlaying,
        iterations = iterations
    )

    LottieAnimation(
        composition = composition,
        progress = { progress.value },
        modifier = modifier
    )
}
