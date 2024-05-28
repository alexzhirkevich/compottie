package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionResult
import io.github.alexzhirkevich.compottie.internal.graphics.draw
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer
import kotlin.math.roundToInt


@Composable
fun rememberLottiePainter(
    composition : LottieCompositionResult,
    progress : () -> Float
) : Painter {

    val painter by  produceState<ProgressPainter>(EmptyPainter){
        value = LottiePainter(composition.await())
    }

    LaunchedEffect(painter){
        snapshotFlow {
            progress()
        }.collect {
            painter.progress = it
        }
    }

    return painter
}

private abstract class ProgressPainter : Painter() {

    abstract var progress : Float
}

private object EmptyPainter : ProgressPainter() {

    override var progress: Float = 0f

    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }

}
private class LottiePainter(
    private val composition: LottieComposition,
) : ProgressPainter() {

    override val intrinsicSize: Size =
        Size(composition.lottieData.width.toFloat(), composition.lottieData.height.toFloat())

    override var progress: Float by mutableStateOf(0f)

    override fun DrawScope.onDraw() {

        val dat = composition.lottieData

        val frame = (dat.outPoint * progress.coerceIn(0f, 1f) - dat.inPoint)
            .coerceAtLeast(0f).roundToInt()

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

        scale(scale.scaleX, scale.scaleY) {
            translate(offset.x.toFloat(), offset.y.toFloat()) {
                dat.layers.fastForEachReversed {
                    when (it) {
                        is ShapeLayer -> it.draw(this, frame)
                    }
                }
            }
        }
    }
}