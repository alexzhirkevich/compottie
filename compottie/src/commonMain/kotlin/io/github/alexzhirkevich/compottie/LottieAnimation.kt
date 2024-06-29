package io.github.alexzhirkevich.compottie

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale


@Composable
@Deprecated(
    "Use Image with rememberLottiePainter(...) instead. Will be removed in 2.0",
    ReplaceWith(
        "Image(rememberLottiePainter(composition,progress),null,modifier,alignment,contentScale)",
        "androidx.compose.foundation.Image",
        "io.github.alexzhirkevich.compottie.rememberLottiePainter"
    )
)
fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds : Boolean = true,
) {
    Image(
        painter = rememberLottiePainter(composition, progress),
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
    )
}

@Deprecated(
    "Use Image with rememberLottiePainter(...) instead.Will be removed in 2.0",
    ReplaceWith(
        "Image(rememberLottiePainter(composition,isPlaying,restartOnPlay,reverseOnRepeat,clipSpec,speed,iterations),null,modifier,alignment,contentScale)",
        "androidx.compose.foundation.Image",
        "io.github.alexzhirkevich.compottie.rememberLottiePainter"
    ),
)
@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    reverseOnRepeat: Boolean = false,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
) {
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            isPlaying = isPlaying,
            restartOnPlay = restartOnPlay,
            reverseOnRepeat = reverseOnRepeat,
            clipSpec = clipSpec,
            speed = speed,
            iterations = iterations,
            clipToCompositionBounds = clipToCompositionBounds
        ),
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
    )
}

