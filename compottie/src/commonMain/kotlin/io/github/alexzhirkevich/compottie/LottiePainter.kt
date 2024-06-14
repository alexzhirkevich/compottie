package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.BaseCompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.CompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.PainterProperties
import kotlin.math.roundToInt

/**
 * Create and remember Lottie painter.
 * Shortcut that combines [rememberLottiePainter] (with progress arg)
 * and [animateLottieCompositionAsState]
 *
 * @param composition [LottieComposition] usually created by [rememberLottieComposition]
 * @param composition The composition to render. This should be retrieved with [rememberLottieComposition].
 * @param isPlaying Whether or not the animation is currently playing. Note that the internal
 * animation may end due to reaching the target iterations count. If that happens, the animation may
 * stop even if this is still true. You can observe the returned [LottieAnimationState.isPlaying]
 * to determine whether the underlying animation is still playing.
 * @param restartOnPlay If isPlaying switches from false to true, restartOnPlay determines whether
 * the progress and iteration gets reset.
 * @param reverseOnRepeat Defines what this animation should do when it reaches the end. This setting
 * is applied only when [iterations] is either greater than 0 or [LottieConstants.IterateForever].
 * Defaults to `false`.
 * @param clipSpec A [LottieClipSpec] that specifies the bound the animation playback
 * should be clipped to.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 * Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param iterations The number of times the animation should repeat before stopping. It must be
 * a positive number. [LottieConstants.IterateForever] can be used to repeat forever.
 * @param cancellationBehavior The behavior that this animation should have when cancelled.
 * In most cases, you will want it to cancel immediately. However, if you have a state based
 * transition and you want an animation to finish playing before moving on to the next one then you
 * may want to set this to [LottieCancellationBehavior.OnIterationFinish].
 * @param useCompositionFrameRate Use frame rate declared in animation instead of screen refresh rate.
 * Animation may seem junky if parameter is set to true and composition frame rate is less than screen
 * refresh rate
 * @param clipToCompositionBounds if animation should be clipped to the
 *  [composition].width x [composition].height
 * @param clipTextToBoundingBoxes if text should be clipped to its bounding boxes (if provided in animation)
 * */
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: LottieClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    useCompositionFrameRate: Boolean = false,
    clipTextToBoundingBoxes: Boolean = false,
    clipToCompositionBounds: Boolean = true
) : Painter {

    val progress = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = restartOnPlay,
        reverseOnRepeat = reverseOnRepeat,
        clipSpec = clipSpec,
        speed = speed,
        iterations = iterations,
        cancellationBehavior = cancellationBehavior,
        useCompositionFrameRate = useCompositionFrameRate
    )

    return rememberLottiePainter(
        composition = composition,
        progress = { progress.value },
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes
    )
}

/**
 * Create and remember Lottie painter
 *
 * @param composition [LottieComposition] usually created by [rememberLottieComposition]
 * @param progress animation progress from 0 to 1 usually derived from [animateLottieCompositionAsState]
 * @param clipToCompositionBounds if drawing should be clipped to the
 *  [composition].width x [composition].height
 * @param clipTextToBoundingBoxes if text should be clipped to its bounding boxes (if provided in animation)
 * */
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    clipToCompositionBounds : Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
) : Painter {

    val fontFamilyResolver = LocalFontFamilyResolver.current

    val painter by produceState<Painter>(
        EmptyPainter,
        composition,
        clipTextToBoundingBoxes,
        clipToCompositionBounds,
        fontFamilyResolver,
    ) {

        if (composition != null) {
            value = LottiePainter(
                composition = composition,
                initialProgress = progress(),
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver,
                clipToCompositionBounds = clipToCompositionBounds
            )
        }
    }

    LaunchedEffect(painter) {
        (painter as? LottiePainter)?.let { lp ->
            snapshotFlow {
                progress()
            }.collect {
                lp.progress = it
            }
        }
    }

    return painter
}

private object EmptyPainter : Painter() {


    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }
}

private class LottiePainter(
    private val composition: LottieComposition,
    initialProgress : Float,
    fontFamilyResolver : FontFamily.Resolver,
    clipTextToBoundingBoxes : Boolean,
    clipToCompositionBounds : Boolean,
) : Painter() {

    override val intrinsicSize: Size = Size(
        composition.lottieData.width,
        composition.lottieData.height
    )

    var progress: Float by mutableStateOf(initialProgress)

    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val compositionLayer: BaseCompositionLayer = composition.lottieData
        .layers
        .takeIf {
            it.size == 1                     // don't create extra composition layer
        }?.first() as? BaseCompositionLayer  // if Precomposition is the only layer
        ?: CompositionLayer(composition)

    init {
        val painterProperties = PainterProperties(
            assets = composition.lottieData.assets.associateBy(LottieAsset::id),
            fontFamilyResolver = fontFamilyResolver,
            clipToDrawBounds = clipToCompositionBounds,
            clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        )
        compositionLayer.painterProperties = painterProperties
        compositionLayer.onCreate(composition)
    }

    private val frame: Float by derivedStateOf {
        val p = composition.lottieData.inPoint +
                (composition.lottieData.outPoint - composition.lottieData.inPoint) * progress
        p.coerceAtLeast(0f)
    }

    override fun applyAlpha(alpha: Float): Boolean {
        if (alpha !in 0f..1f)
            return false

        this.alpha = alpha
        return true
    }

    private val animationState = AnimationState(frame, composition)

    override fun DrawScope.onDraw() {

        matrix.reset()

        val scale = ContentScale.Fit.computeScaleFactor(intrinsicSize, size)

        val offset = Alignment.Center.align(
            IntSize(
                (intrinsicSize.width).roundToInt(),
                (intrinsicSize.height).roundToInt()
            ),
            IntSize(
                (size.width).roundToInt(),
                (size.height).roundToInt()
            ),
            layoutDirection
        )

        matrix.reset()

        scale(scale.scaleX, scale.scaleY) {
            translate(offset.x.toFloat(), offset.y.toFloat()) {
                animationState.remapped(frame) {
                    compositionLayer.draw(this, matrix, alpha, it)
                }
            }
        }
    }
}

