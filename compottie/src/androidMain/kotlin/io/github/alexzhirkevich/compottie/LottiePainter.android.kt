package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * A composable that makes it easy to create a [LottiePainter] and update its properties.
 */
@Composable
actual fun rememberLottiePainter(
    composition: LottieComposition?,
    progress: Float,
    clipToCompositionBounds: Boolean,
): LottiePainter = com.airbnb.lottie.compose.rememberLottiePainter(
    composition = composition,
    progress = progress,
    clipToCompositionBounds = clipToCompositionBounds,
)

actual typealias LottiePainter = com.airbnb.lottie.compose.LottiePainter
