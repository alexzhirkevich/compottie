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
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.services.LottieImageService
import io.github.alexzhirkevich.compottie.internal.services.LottieServiceLocator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.math.roundToInt


@Composable
fun rememberLottiePainter(
    composition : LottieCompositionResult,
    maintainOriginalImageBounds: Boolean = false,
    onLoadError : (Throwable) -> Painter =  { EmptyPainter },
    progress : () -> Float
) : Painter {

    val painter by  produceState<Painter>(EmptyPainter) {
        value = try {
            LottiePainter(
                composition = composition.await(),
                maintainOriginalImageBounds = maintainOriginalImageBounds
            )
        } catch (t : Throwable) {
            onLoadError(t)
        }
    }

    LaunchedEffect(painter){
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
    private val maintainOriginalImageBounds : Boolean,
) : Painter() {

    override val intrinsicSize: Size = Size(
        composition.lottieData.width.toFloat(),
        composition.lottieData.height.toFloat()
    )

    var progress: Float by mutableStateOf(0f)

    private val matrix = Matrix()

    private var alpha by mutableStateOf(1f)

    private val currentFrame by derivedStateOf {
        val p = composition.lottieData.outPoint * progress.coerceIn(0f, 1f) -
                composition.lottieData.inPoint
        p.coerceAtLeast(0f).roundToInt()
    }

    private var serviceLocator = LottieServiceLocator(
        LottieImageService(
            maintainOriginalImageBounds = maintainOriginalImageBounds,
            assets = composition.lottieData.assets
        )
    )

    init {
        composition.lottieData.layers.forEach {
            it.serviceLocator = serviceLocator
        }
    }

    override fun applyAlpha(alpha: Float): Boolean {
        if (alpha !in 0f..1f)
            return false

        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {

        drawIntoCanvas { canvas ->

            matrix.reset()

            val scale = ContentScale.Fit.computeScaleFactor(intrinsicSize, size)

            val offset = Alignment.Center.align(
                IntSize(
                    (intrinsicSize.width).roundToInt(),
                    (intrinsicSize.height).roundToInt()
                ),
                IntSize(
                    size.width.roundToInt(),
                    size.height.roundToInt()
                ),
                layoutDirection
            )

//            scale(scale.scaleX, scale.scaleY) {
//                translate(offset.x.toFloat(), offset.y.toFloat()) {
                    composition.lottieData.layers.fastForEachReversed {
                        if (it is DrawingContent) {
                            it.density = density
                            try {
                                it.draw(canvas, matrix, alpha, currentFrame)
                            } catch (t: Throwable) {
                                println("Lottie crashed in draw :(")
                                t.printStackTrace()
                            }
//                        }
//                    }
                }
            }
        }
    }
}