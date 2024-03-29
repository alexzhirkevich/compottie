package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import com.airbnb.lottie.LottieComposition

actual typealias LottieComposition = LottieComposition

internal actual val LottieComposition.fps: Float
    get() = frameRate

internal actual val LottieComposition.durationMillis: Float
    get() = duration


internal actual val LottieComposition.lastFrame: Float
    get() = endFrame

internal actual fun LottieComposition.marker(markerName : String) : Marker? =
    getMarker(markerName)


@Composable
actual fun rememberLottieComposition(spec: LottieCompositionSpec) : LottieCompositionResult =
    com.airbnb.lottie.compose.rememberLottieComposition(
        spec = spec.delegate,
        imageAssetsFolder = null
    )

