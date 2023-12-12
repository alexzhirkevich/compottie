package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

//actual typealias LottieCancellationBehavior = com.airbnb.lottie.compose.LottieCancellationBehavior


actual class LottieComposition(
    internal val composition : com.airbnb.lottie.LottieComposition
) {

    actual val frameRate: Float
        get() = composition.frameRate

    actual val duration: Float
        get() = composition.duration / 1000
}

//actual val LottieComposition.duration : Float
//    get() = duration / 1000f

@Composable
actual fun rememberLottieComposition(data : String) : State<LottieComposition?> {

    val state = remember {
        mutableStateOf<LottieComposition?>(null)
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(data))

    LaunchedEffect(composition) {
//        composition?.b
        state.value = composition?.let(::LottieComposition)
    }

    return state
}

@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier,
) {
    com.airbnb.lottie.compose.LottieAnimation(
        modifier = modifier,
        composition = composition?.composition,
        progress = progress
    )
}

@Composable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    repeatMode: RepeatMode,
    cancellationBehavior: CancellationBehavior,
    isPlaying : Boolean,
    iterations : Int,
) : State<Float> {
    return animateLottieCompositionAsState(
        composition = composition?.composition,
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