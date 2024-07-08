package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import io.github.alexzhirkevich.compottie.assets.EmptyAssetsManager
import io.github.alexzhirkevich.compottie.assets.EmptyFontManager
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.dynamic.LottieDynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.CompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt
import kotlin.time.measureTime

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
 * @param enableExpressions enable experimental expressions feature. Unsupported expressions will
 * be skipped with warning.
 * */
@OptIn(InternalCompottieApi::class)
@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    assetsManager: LottieAssetsManager? = null,
    fontManager: LottieFontManager? = null,
    dynamicProperties : LottieDynamicProperties? = null,
    applyOpacityToLayers : Boolean = false,
    clipToCompositionBounds : Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
    enableMergePaths: Boolean = false,
    enableExpressions: Boolean = true,
) : Painter {

    val fontFamilyResolver = LocalFontFamilyResolver.current

    val updatedProgress by rememberUpdatedState(progress)

    val painter by produceState<Painter>(
        EmptyPainter,
        composition,
    ) {
        if (composition != null) {
            val assets = async(ioDispatcher()) {
                composition.loadAssets(assetsManager ?: EmptyAssetsManager, true)
            }
            val fonts = async(ioDispatcher()) {
                composition.loadFonts(fontManager ?: EmptyFontManager)
            }

            value = LottiePainter(
                composition = composition.deepCopy(),
                progress = { updatedProgress() },
                dynamicProperties = when (dynamicProperties) {
                    is DynamicCompositionProvider -> dynamicProperties
                    null -> null
                },
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver,
                clipToCompositionBounds = clipToCompositionBounds,
                enableMergePaths = enableMergePaths,
                enableExpressions = enableExpressions,
                applyOpacityToLayers = applyOpacityToLayers,
                assets = assets.await(),
                fonts = fonts.await()
            )
        }
    }

    LaunchedEffect(
        painter,
        fontFamilyResolver,
        clipTextToBoundingBoxes,
        clipToCompositionBounds,
        applyOpacityToLayers,
        enableMergePaths,
        enableExpressions
    ) {
        (painter as? LottiePainter)?.let {
            it.enableMergePaths = enableMergePaths
            it.enableExpressions = enableExpressions
            it.applyOpacityToLayers = applyOpacityToLayers
            it.clipToCompositionBounds = clipToCompositionBounds
            it.clipTextToBoundingBoxes = clipTextToBoundingBoxes
            it.fontFamilyResolver = fontFamilyResolver
        }
    }

    LaunchedEffect(
        painter,
        dynamicProperties
    ) {
        (painter as? LottiePainter)?.setDynamicProperties(
            when (dynamicProperties) {
                is DynamicCompositionProvider -> dynamicProperties
                null -> null
            },
        )
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
    assetsManager: LottieAssetsManager? = null,
    fontManager: LottieFontManager? = null,
    dynamicProperties : LottieDynamicProperties? = null,
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
    enableExpressions: Boolean = true,
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
        applyOpacityToLayers = applyOpacityToLayers,
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths,
        enableExpressions = enableExpressions
    )
}


/**
 * Factory method to create Lottie painter from non-composable context.
 * This painter will not work with Android Studio preview.
 * Use [rememberLottiePainter] to create it from the composition.
 *
 * [progress] lambda has to be derivable so that [derivedStateOf] can derive progress from it.
 *
 * Use [LottieCompositionSpec.load] to get [LottieComposition] instance from [LottieCompositionSpec].
 * */
@OptIn(InternalCompottieApi::class)
suspend fun LottiePainter(
    composition : LottieComposition,
    progress : () -> Float,
    assetsManager: LottieAssetsManager? = null,
    fontManager: LottieFontManager? = null,
    dynamicProperties : LottieDynamicProperties? = null,
    applyOpacityToLayers : Boolean = false,
    clipToCompositionBounds : Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
    enableMergePaths: Boolean = false,
    enableExpressions: Boolean = true,
) : Painter = coroutineScope {
    val assets = async(ioDispatcher()) {
        assetsManager?.let {
            composition.loadAssets(it, true)
        }
    }
    val fonts = async(ioDispatcher()) {
        fontManager?.let {
            composition.loadFonts(it)
        }
    }

    LottiePainter(
        composition = composition.deepCopy(),
        progress = progress,
        dynamicProperties = when (dynamicProperties) {
            is DynamicCompositionProvider -> dynamicProperties
            null -> null
        },
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        fontFamilyResolver = makeFontFamilyResolver(),
        clipToCompositionBounds = clipToCompositionBounds,
        enableMergePaths = enableMergePaths,
        enableExpressions = enableExpressions,
        applyOpacityToLayers = applyOpacityToLayers,
        assets = assets.await().orEmpty(),
        fonts = fonts.await().orEmpty()
    )
}

internal expect fun makeFontFamilyResolver() : FontFamily.Resolver
internal expect fun mockFontFamilyResolver() : FontFamily.Resolver

private object EmptyPainter : Painter() {


    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }
}

private class LottiePainter(
    private val composition: LottieComposition,
    progress : () -> Float,
    assets : List<LottieAsset>,
    fonts : Map<String, FontFamily>,
    dynamicProperties: DynamicCompositionProvider?,
    fontFamilyResolver : FontFamily.Resolver,
    applyOpacityToLayers : Boolean,
    clipTextToBoundingBoxes : Boolean,
    clipToCompositionBounds : Boolean,
    enableMergePaths : Boolean,
    enableExpressions : Boolean,
) : Painter() {


    override val intrinsicSize: Size = Size(
        composition.animation.width,
        composition.animation.height
    )

    private val intIntrinsicSize = IntSize(
        intrinsicSize.width.roundToInt(),
        intrinsicSize.height.roundToInt()
    )

    private val progress: Float by derivedStateOf(progress::invoke)

    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val compositionLayer: Layer = CompositionLayer(composition)

    private val frame: Float by derivedStateOf {
        lerp(composition.startFrame, composition.endFrame, this.progress)
    }

    private val animationState = AnimationState(
        composition = composition,
        assets = assets.associateBy(LottieAsset::id),
        fonts = fonts,
        frame = frame,
        fontFamilyResolver = fontFamilyResolver,
        applyOpacityToLayers = applyOpacityToLayers,
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBoxes = clipTextToBoundingBoxes,
        enableMergePaths = enableMergePaths,
        layer = compositionLayer,
        enableExpressions = enableExpressions
    )

    fun setDynamicProperties(provider: DynamicCompositionProvider?) {
        compositionLayer.setDynamicProperties(provider, animationState)
    }

    init {
        setDynamicProperties(dynamicProperties)
    }

    var applyOpacityToLayers: Boolean by animationState::applyOpacityToLayers
    var clipTextToBoundingBoxes: Boolean by animationState::clipTextToBoundingBoxes
    var clipToCompositionBounds: Boolean by animationState::clipToCompositionBounds
    var fontFamilyResolver: FontFamily.Resolver by animationState::fontFamilyResolver
    var enableMergePaths: Boolean by animationState::enableMergePaths
    var enableExpressions: Boolean by animationState::enableExpressions

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