package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
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
import kotlin.time.measureTime

@OptIn(InternalCompottieApi::class)
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
    useCompositionFrameRate: Boolean = false
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
    )
}

@Composable
fun rememberLottiePainter(
    composition : LottieComposition?,
    progress : () -> Float,
    clipTextToBoundingBoxes: Boolean = false
) : Painter {

    val fontFamilyResolver = LocalFontFamilyResolver.current

    val painter by produceState<Painter>(
        EmptyPainter,
        composition,
        clipTextToBoundingBoxes,
        fontFamilyResolver
    ) {

        if (composition != null) {
            value = LottiePainter(
                composition = composition,
                clipTextToBoundingBoxes = clipTextToBoundingBoxes,
                fontFamilyResolver = fontFamilyResolver
            )
        }
    }

    LaunchedEffect(painter) {
        snapshotFlow {
            progress()
        }.collect {
            (painter as? LottiePainter)?.progress = it
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
    private val fontFamilyResolver : FontFamily.Resolver,
    private val clipTextToBoundingBoxes : Boolean,
) : Painter() {

    override val intrinsicSize: Size = Size(
        composition.lottieData.width,
        composition.lottieData.height
    )

    var progress: Float by mutableStateOf(0f)

    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val currentFrame by derivedStateOf {
        val p = composition.lottieData.outPoint * progress.coerceIn(0f, 1f) -
                composition.lottieData.inPoint
        p.coerceAtLeast(0f)
    }

    val compositionLayer: BaseCompositionLayer = composition.lottieData
        .layers
        .takeIf {
            it.size == 1                     // don't create extra composition layer
        }?.first() as? BaseCompositionLayer  // if Precomposition is the only layer
        ?: CompositionLayer(composition)


    init {
        compositionLayer.painterProperties = PainterProperties(
            assets = composition.lottieData.assets.associateBy(LottieAsset::id),
            composition = composition,
            clipTextToBoundingBoxes = clipTextToBoundingBoxes,
            fontFamilyResolver = fontFamilyResolver
        )
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
                measureTime {
                    compositionLayer.draw(this, matrix, alpha, AnimationState(frame))
                }.also {
                    println(it.inWholeMilliseconds)
                }
            }
        }
    }
}

