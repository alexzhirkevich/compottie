package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * A composable that makes it easy to create a [LottiePainter] and update its properties.
 */
@Composable
expect fun rememberLottiePainter(
    composition: LottieComposition? = null,
    progress: Float = 0f,
    clipToCompositionBounds: Boolean = true,
): LottiePainter

expect class LottiePainter : Painter
