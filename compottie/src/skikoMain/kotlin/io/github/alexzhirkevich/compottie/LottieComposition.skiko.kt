package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController

actual class LottieComposition internal constructor(
    internal val animation: Animation,
    internal val invalidationController: InvalidationController = InvalidationController()
)

actual val LottieComposition.fps: Float
    get() = animation.fPS

actual val LottieComposition.durationMillis: Float
    get() = animation.duration * 1000

internal val LottieComposition.endFrame : Float
    get() = animation.fPS * animation.duration


@Composable
actual fun rememberLottieComposition(data : String) : State<LottieComposition?> {

    val composition = remember {
        mutableStateOf<LottieComposition?>(null)
    }

    LaunchedEffect(data){
        withContext(Dispatchers.Default){
            composition.value = LottieComposition(Animation.makeFromString(data))
        }
    }

    return composition
}
