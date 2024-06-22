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
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.DynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.CompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.Layer
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
 * @param enableMergePaths enable experimental merge paths feature. Most of the time animation doesn't need
 * it even if it contains merge paths. This feature should only be enabled for tested animations
 * */
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    dynamicProperties : DynamicProperties? = null,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: LottieClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    useCompositionFrameRate: Boolean = false,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
    enableMergePaths: Boolean = false,
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
        dynamicProperties = dynamicProperties,
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths
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
 * @param enableMergePaths enable experimental merge paths feature. Most of the time animation doesn't need
 * it even if it contains merge paths. This feature should only be enabled for tested animations
 * */
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    dynamicProperties : DynamicProperties? = null,
    clipToCompositionBounds : Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
    enableMergePaths: Boolean = false,
) : Painter {

    val fontFamilyResolver = LocalFontFamilyResolver.current

    val painter by produceState<Painter>(
        EmptyPainter,
        composition,
    ) {

        if (composition != null) {
            value = LottiePainter(
                composition = composition,
                initialProgress = progress(),
                dynamicProperties = when (dynamicProperties) {
                    is DynamicCompositionProvider -> dynamicProperties
                    null -> dynamicProperties
                },
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver,
                clipToCompositionBounds = clipToCompositionBounds,
                enableMergePaths = enableMergePaths
            )
        }
    }

    LaunchedEffect(
        clipTextToBoundingBoxes,
        clipToCompositionBounds,
        fontFamilyResolver,
    ){
        (painter as? LottiePainter)?.let {
            it.clipTextToBoundingBoxes = clipTextToBoundingBoxes
            it.clipToCompositionBounds = clipToCompositionBounds
            it.fontFamilyResolver = fontFamilyResolver
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
    dynamicProperties: DynamicCompositionProvider?,
    fontFamilyResolver : FontFamily.Resolver,
    clipTextToBoundingBoxes : Boolean,
    clipToCompositionBounds : Boolean,
    enableMergePaths : Boolean,
) : Painter() {

    var progress: Float by mutableStateOf(initialProgress)

    override val intrinsicSize: Size = Size(
        composition.animation.width,
        composition.animation.height
    )

    private val intIntrinsicSize = IntSize(
        intrinsicSize.width.roundToInt(),
        intrinsicSize.height.roundToInt()
    )



    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val compositionLayer: Layer = CompositionLayer(composition)

    private val frame: Float by derivedStateOf {
        lerp(composition.startFrame, composition.endFrame, progress)
    }

    private val animationState = AnimationState(
        frame = frame,
        composition = composition,
        fontFamilyResolver = fontFamilyResolver,
        clipToDrawBounds = clipToCompositionBounds,
        dynamicProperties = dynamicProperties,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths,
        layer = compositionLayer,
        assets = composition.animation.assets.associateBy(LottieAsset::id)
    )

    var clipTextToBoundingBoxes: Boolean by animationState::clipTextToBoundingBoxes
    var clipToCompositionBounds: Boolean by animationState::clipToCompositionBounds
    var fontFamilyResolver: FontFamily.Resolver by animationState::fontFamilyResolver
    var dynamic: DynamicCompositionProvider? by animationState::dynamic

    override fun applyAlpha(alpha: Float): Boolean {
        if (alpha !in 0f..1f)
            return false

        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {
        matrix.reset()

        val scale = ContentScale.Fit.computeScaleFactor(intrinsicSize, size)

        val offset = Alignment.Center.align(
            size = intIntrinsicSize,
            space = IntSize(
                size.width.roundToInt(),
                size.height.roundToInt()
            ),
            layoutDirection = layoutDirection
        )

        scale(scale.scaleX, scale.scaleY) {
            translate(offset.x.toFloat(), offset.y.toFloat()) {
                animationState.onFrame(frame) {
                    compositionLayer.draw(this, matrix, alpha, it)
                }
            }
        }
    }
}

