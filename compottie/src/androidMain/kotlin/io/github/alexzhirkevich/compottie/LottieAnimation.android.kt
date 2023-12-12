package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

//actual typealias LottieCancellationBehavior = com.airbnb.lottie.compose.LottieCancellationBehavior


actual class LottieComposition(
    internal val composition : com.airbnb.lottie.LottieComposition?
)

//actual val LottieComposition.duration : Float
//    get() = duration / 1000f

@Composable
actual fun rememberLottieComposition(data : String) : LottieComposition {
    val composition = rememberLottieComposition(LottieCompositionSpec.JsonString(data)).value

    return remember(composition) {
        LottieComposition(composition)
    }
}

@Composable
actual fun LottieAnimation(
    composition : LottieComposition,
    progress : () -> Float,
    modifier: Modifier,
) {
    com.airbnb.lottie.compose.LottieAnimation(
        modifier = modifier,
        composition = composition.composition,
        progress = progress
    )
}

@Composable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition,
    repeatMode: RepeatMode,
    cancellationBehavior: CancellationBehavior,
    isPlaying : Boolean,
    iterations : Int,
) : State<Float> {
    return animateLottieCompositionAsState(
        composition = composition.composition,
        reverseOnRepeat = repeatMode == RepeatMode.Reverse,
        iterations = iterations,
        restartOnPlay = true,
        isPlaying = isPlaying,
        cancellationBehavior = when (cancellationBehavior) {
            CancellationBehavior.Immediately -> LottieCancellationBehavior.Immediately
            CancellationBehavior.OnIterationFinish -> LottieCancellationBehavior.OnIterationFinish
        },
    )
}