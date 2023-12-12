package io.github.alexzhirkevich.compottie

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController
import kotlin.math.roundToInt

actual class LottieComposition(internal val animation: Animation) {
    actual val frameRate: Float
        get() = animation.fPS

    /**
     * Animation duration in seconds
     * */
    actual val duration: Float
        get() = animation.duration
}

//actual val LottieComposition.duration : Float
//    get() = duration

@Composable
actual fun rememberLottieComposition(data : String) : State<LottieComposition?> {
    return remember(data) { mutableStateOf(LottieComposition(Animation.makeFromString(data))) }
}

@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier
) {

    val defaultSize = LocalDensity.current.run {
        if (composition == null)
            DpSize.Zero
        else
            DpSize(
                composition.animation.width.toDp(),
                composition.animation.height.toDp(),
            )
    }


    val invalidationController = remember { InvalidationController() }

    Canvas(
        modifier
            .size(defaultSize)
    ) {
        drawIntoCanvas {
            if (composition != null) {
                composition.animation.seek(progress(), invalidationController)

                composition.animation.render(
                    canvas = it.nativeCanvas,
                    dst = Rect.makeWH(size.width, size.height)
                )
            }
        }
    }
}

@Composable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    repeatMode: RepeatMode,
    cancellationBehavior: CancellationBehavior,
    isPlaying : Boolean,
    iterations : Int,
) : State<Float> {

    val duration = composition?.animation?.duration?.times(1000)?.roundToInt() ?: 0
    val animationSpec = tween<Float>(duration, easing = LinearEasing)

    val progress = remember {
        mutableFloatStateOf(0f)
    }

    val isPlayingUpdated by rememberUpdatedState(isPlaying)
    val cancellationBehaviorUpdated by rememberUpdatedState(cancellationBehavior)

    val isAnimationStopped by remember {
        derivedStateOf {
            !isPlayingUpdated && cancellationBehavior == CancellationBehavior.Immediately
        }
    }

    val shouldTakeProgress = { new: Float ->
        if (!isPlayingUpdated && cancellationBehaviorUpdated == CancellationBehavior.Immediately)
            false
        else {
            if (isPlayingUpdated)
                true
            else {
                new >= progress.value
            }
        }
    }


    when {
        isAnimationStopped -> {
            LaunchedEffect(0) {
                progress.value = 0f
            }
        }

        iterations == Int.MAX_VALUE -> {
            val infiniteTransition = rememberInfiniteTransition()

            val p = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = animationSpec,
                    repeatMode = repeatMode
                )
            )

            LaunchedEffect(0) {
                snapshotFlow { p.value }
                    .takeWhile { shouldTakeProgress(it) }
                    .onCompletion { progress.value = 0f }
                    .collectLatest { progress.value = it }
            }
        }

        else -> {
            var v by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(v) {
                v = 1f
            }

            LaunchedEffect(progress) {
                snapshotFlow { v }
                    .takeWhile { shouldTakeProgress(it) }
                    .onCompletion { progress.value = 0f }
                    .collectLatest { progress.value = v }
            }

            animateFloatAsState(
                targetValue = v,
                animationSpec = repeatable(
                    iterations = iterations,
                    animation = animationSpec,
                    repeatMode = repeatMode
                )
            )
        }
    }

    return progress
}