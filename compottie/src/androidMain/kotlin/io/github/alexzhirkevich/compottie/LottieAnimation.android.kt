package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

actual typealias LottieCancellationBehavior = com.airbnb.lottie.compose.LottieCancellationBehavior

actual typealias LottieComposition = com.airbnb.lottie.LottieComposition

//actual val LottieComposition.duration : Float
//    get() = duration / 1000f

@Composable
actual fun rememberLottieComposition(data : String) : LottieComposition? {
    return rememberLottieComposition(LottieCompositionSpec.JsonString(data)).value
}

@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier,
) {
    com.airbnb.lottie.compose.LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = progress
    )
}

@Composable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    repeatMode: RepeatMode,
    cancellationBehavior: LottieCancellationBehavior,
    isPlaying : Boolean,
    iterations : Int,
) : State<Float> {
    return animateLottieCompositionAsState(
        composition = composition,
        reverseOnRepeat = repeatMode == RepeatMode.Reverse,
        iterations = iterations,
        restartOnPlay = true,
        isPlaying = isPlaying,
        cancellationBehavior = cancellationBehavior,
    )
}