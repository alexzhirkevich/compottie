package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

actual typealias LottieClipSpec = com.airbnb.lottie.compose.LottieClipSpec

actual object LottieClipSpecs {
    actual fun Progress(
        min: Float,
        max: Float
    ): LottieClipSpec =
        com.airbnb.lottie.compose.LottieClipSpec.Progress(min, max)


    actual fun Frame(
        min: Int?,
        max: Int?,
        maxInclusive: Boolean
    ): LottieClipSpec =
        com.airbnb.lottie.compose.LottieClipSpec.Frame(min, max, maxInclusive)
}

actual typealias LottieConstants = com.airbnb.lottie.compose.LottieConstants

actual typealias LottieCancellationBehavior = com.airbnb.lottie.compose.LottieCancellationBehavior

actual typealias LottieAnimationState = com.airbnb.lottie.compose.LottieAnimationState

actual typealias LottieComposition  = com.airbnb.lottie.LottieComposition

actual val LottieComposition.fps: Float
    get() = frameRate

/**
 * Animation duration in milliseconds
 * */
actual val LottieComposition.durationMillis: Float
    get() = duration

@Composable
actual fun rememberLottieComposition(data : String) : State<LottieComposition?> {
    return rememberLottieComposition(LottieCompositionSpec.JsonString(data))
}

@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier,
) = com.airbnb.lottie.compose.LottieAnimation(
    modifier = modifier,
    composition = composition,
    progress = progress
)

@Composable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    isPlaying: Boolean,
    restartOnPlay: Boolean,
    reverseOnRepeat: Boolean,
    clipSpec: LottieClipSpec?,
    speed: Float,
    iterations: Int,
    cancellationBehavior: LottieCancellationBehavior,
    ignoreSystemAnimatorScale: Boolean,
    useCompositionFrameRate: Boolean,
) : LottieAnimationState =  animateLottieCompositionAsState(
    composition = composition,
    isPlaying = isPlaying,
    restartOnPlay = restartOnPlay,
    reverseOnRepeat = reverseOnRepeat,
    clipSpec = clipSpec,
    speed = speed,
    iterations = iterations,
    cancellationBehavior = cancellationBehavior,
    ignoreSystemAnimatorScale = ignoreSystemAnimatorScale
)

