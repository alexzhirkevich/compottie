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
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.dynamic.DynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.CompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.coroutines.async
import kotlin.math.roundToInt

/**
 * Create and remember Lottie painter
 *
 * @param composition [LottieComposition] usually created by [rememberLottieComposition]
 * @param progress animation progress from 0 to 1 usually derived from [animateLottieCompositionAsState]
 * @param assetsManager used to load animation assets that were not loaded during composition
 * initialization
 * @param fontManager used to load animation fonts
 * @param dynamicProperties dynamically-configurable animation properties. Can be created with
 * [rememberLottieDynamicProperties]
 * @param applyOpacityToLayers Sets whether to apply opacity to the each layer instead of shape.
 * Opacity is normally applied directly to a shape. In cases where translucent
 * shapes overlap, applying opacity to a layer will be more accurate at the expense of performance.
 * Note: Turning this on can be very expensive and sometimes can cause artifacts. Enable it only if
 * the animation have translucent overlapping shapes and always test if it works fine for your animation
 * @param clipToCompositionBounds if drawing should be clipped to the [composition].width X [composition].height rect
 * @param clipTextToBoundingBoxes if text should be clipped to its bounding boxes (if provided in animation)
 * @param enableMergePaths enable experimental merge paths feature. Most of the time animation doesn't need
 * it even if it contains merge paths. This feature should only be enabled for tested animations
 * */
@OptIn(InternalCompottieApi::class)
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    assetsManager: LottieAssetsManager = LottieAssetsManager.Empty,
    fontManager: LottieFontManager = LottieFontManager.Empty,
    dynamicProperties : DynamicProperties? = null,
    applyOpacityToLayers : Boolean = false,
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
            val assets = async(ioDispatcher()) {
                composition.loadAssets(assetsManager, true)
            }
            val fonts = async(ioDispatcher()) {
                composition.loadFonts(fontManager)
            }

            value = LottiePainter(
                composition = composition.deepCopy(),
                initialProgress = progress(),
                dynamicProperties = when (dynamicProperties) {
                    is DynamicCompositionProvider -> dynamicProperties
                    null -> null
                },
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver,
                clipToCompositionBounds = clipToCompositionBounds,
                enableMergePaths = enableMergePaths,
                applyOpacityToLayers = applyOpacityToLayers,
                assets = assets.await(),
                fonts = fonts.await()
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

/**
 * Create and remember Lottie painter.
 *
 * Shortcut that combines [rememberLottiePainter] and [animateLottieCompositionAsState]
 * */
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    assetsManager: LottieAssetsManager = LottieAssetsManager.Empty,
    fontManager: LottieFontManager = LottieFontManager.Empty,
    dynamicProperties : DynamicProperties? = null,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    applyOpacityToLayers : Boolean = false,
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
        assetsManager = assetsManager,
        fontManager = fontManager,
        dynamicProperties = dynamicProperties,
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths
    )
}

private object EmptyPainter : Painter() {


    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }
}

private class LottiePainter(
    private val composition: LottieComposition,
    assets : List<LottieAsset>,
    fonts : Map<String, FontFamily>,
    initialProgress : Float,
    dynamicProperties: DynamicCompositionProvider?,
    fontFamilyResolver : FontFamily.Resolver,
    applyOpacityToLayers : Boolean,
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
        composition = composition,
        assets = assets.associateBy(LottieAsset::id),
        fonts = fonts,
        frame = frame,
        fontFamilyResolver = fontFamilyResolver,
        applyOpacityToLayers = applyOpacityToLayers,
        clipToDrawBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths,
        layer = compositionLayer
    )

    init {
        if (dynamicProperties != null) {
            compositionLayer.setDynamicProperties(dynamicProperties, animationState)
        }
    }

    var clipTextToBoundingBoxes: Boolean by animationState::clipTextToBoundingBoxes
    var clipToCompositionBounds: Boolean by animationState::clipToCompositionBounds
    var fontFamilyResolver: FontFamily.Resolver by animationState::fontFamilyResolver

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

