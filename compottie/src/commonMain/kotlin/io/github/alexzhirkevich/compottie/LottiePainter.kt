package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.assets.EmptyAssetsManager
import io.github.alexzhirkevich.compottie.assets.EmptyFontManager
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.LottieDynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionsEngine
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.CompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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
 * @param enableTextGrouping disable line-to-char splitting. Enable this to correctly render texts
 * in locales such as Arabic. This feature forces to use fonts over glyphs and disables text tracking.
 * However, if you have texts rendered with fonts and don't use tracking, you can try enable this option
 * for any locales as this feature greatly improves texts performance
 * @param enableMergePaths enable experimental merge paths feature. Most of the time animation doesn't need
 * it even if it contains merge paths. This feature should only be enabled for tested animations
 * @param enableExpressions enable experimental expressions feature. Changing this parameter after
 * composition (with recomposition) may cause performance spike
 * */
@OptIn(InternalCompottieApi::class)
@Composable
public fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    assetsManager: LottieAssetsManager? = null,
    fontManager: LottieFontManager? = null,
    coroutineContext: CoroutineContext = Compottie.ioDispatcher(),
    dynamicProperties : LottieDynamicProperties? = null,
    applyOpacityToLayers : Boolean = false,
    clipToCompositionBounds : Boolean = true,
    clipTextToBoundingBoxes: Boolean = false,
    enableTextGrouping : Boolean = false,
    enableMergePaths: Boolean = false,
    enableExpressions: Boolean = false
) : Painter {

    val fontFamilyResolver = LocalFontFamilyResolver.current

    val updatedProgress by rememberUpdatedState(progress)

    val dp = when (dynamicProperties) {
        is DynamicCompositionProvider -> dynamicProperties
        null -> null
    }

    val copy = dp != null

    val coroutineScope = rememberCoroutineScope()

    val painter by produceState<LottiePainter?>(
        null, composition, copy, coroutineScope
    ) {
        if (composition != null) {
            val assets = if (composition.hasAssets) {
                async(coroutineContext) {
                    composition.loadAssets(assetsManager ?: EmptyAssetsManager, copy)
                }
            } else {
                null
            }

            val fonts = if (composition.hasFonts) {
                async(coroutineContext) {
                    composition.loadFonts(fontManager ?: EmptyFontManager)
                }
            } else {
                null
            }

            val comp = if (copy) composition.deepCopy() else composition


            val painter = LottiePainter(
                composition = comp,
                progress = updatedProgress::invoke,
                dynamicProperties = dp,
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver,
                clipToCompositionBounds = clipToCompositionBounds,
                enableTextGrouping = enableTextGrouping,
                enableMergePaths = enableMergePaths,
                enableExpressions = enableExpressions,
                applyOpacityToLayers = applyOpacityToLayers,
                assets = assets?.await().orEmpty(),
                fonts = fonts?.await().orEmpty(),
                coroutineContext = coroutineScope.coroutineContext
            )

            if (enableExpressions) {
                withContext(coroutineContext) {
                    composition.animation.prepareExpressions(painter.animationState)
                }
            }

            value = painter
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
        painter?.let {
            it.enableMergePaths = enableMergePaths
            it.enableExpressions = enableExpressions
            it.applyOpacityToLayers = applyOpacityToLayers
            it.clipToCompositionBounds = clipToCompositionBounds
            it.clipTextToBoundingBoxes = clipTextToBoundingBoxes
            it.fontFamilyResolver = fontFamilyResolver
        }
    }

    LaunchedEffect(painter, dp) {
        painter?.setDynamicProperties(dp)
    }

    return remember {
        LateInitPainter { painter }
    }
}

/**
 * Create and remember Lottie painter.
 *
 * Shortcut that combines [rememberLottiePainter] and [animateLottieCompositionAsState]
 * */
@Composable
public fun rememberLottiePainter(
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
    enableExpressions: Boolean = false
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
        progress = progress::value,
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


internal expect fun mockFontFamilyResolver() : FontFamily.Resolver

private class LateInitPainter(
    val painter : () -> LottiePainter?
) : Painter() {

    private var alpha by mutableStateOf(1f)
    private var colorFilter by mutableStateOf<ColorFilter?>(null)

    override val intrinsicSize: Size by derivedStateOf {
        painter()?.intrinsicSize ?: Size(1f,1f)
    }

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        painter()?.run {
            draw(size, alpha, colorFilter)
        }
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
    enableTextGrouping : Boolean,
    clipToCompositionBounds : Boolean,
    enableMergePaths : Boolean,
    enableExpressions : Boolean,
    coroutineContext: CoroutineContext
) : Painter() {


    override val intrinsicSize: Size = Size(
        composition.animation.width,
        composition.animation.height
    )

    private val progress: Float by derivedStateOf(progress::invoke)

    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val compositionLayer: Layer = CompositionLayer(composition)

    private val frame: Float by derivedStateOf {
        lerp(composition.startFrame, composition.endFrame, this.progress)
    }

    val animationState = AnimationState(
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
        enableExpressions = enableExpressions,
        enableTextGrouping = enableTextGrouping,
        coroutineContext = coroutineContext
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

    public override fun applyAlpha(alpha: Float): Boolean {
        if (alpha !in 0f..1f)
            return false

        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {
        try {

            matrix.fastReset()
            matrix.preScale(
                size.width / intrinsicSize.width,
                size.height / intrinsicSize.height
            )

            animationState.onFrame(frame) {
                compositionLayer.draw(this, matrix, alpha, it)
            }
        } catch (t: Throwable) {
            Compottie.logger?.error("Lottie crashed in draw :C", t)
        }
    }
}