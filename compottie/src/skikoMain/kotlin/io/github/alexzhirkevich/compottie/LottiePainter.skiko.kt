package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.toOffset
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.RenderFlag

/**
 * A composable that makes it easy to create a [LottiePainter] and update its properties.
 */
@Composable
actual fun rememberLottiePainter(
    composition: LottieComposition?,
    progress: Float,
    clipToCompositionBounds: Boolean,
): LottiePainter {
    val painter = remember { LottiePainter() }
    painter.composition = composition
    painter.progress = progress
    painter.clipToCompositionBounds = clipToCompositionBounds
    return painter
}

actual class LottiePainter internal constructor(
    composition: LottieComposition? = null,
    progress: Float = 0f,
    clipToCompositionBounds: Boolean = true,
) : Painter() {
    internal var composition by mutableStateOf(composition)
    internal var progress by mutableFloatStateOf(progress)
    internal var clipToCompositionBounds by mutableStateOf(clipToCompositionBounds)

    override val intrinsicSize: Size
        get() {
            val composition = composition ?: return Size.Unspecified
            return Size(composition.animation.width, composition.animation.height)
        }

    private val defaultFlags = listOf(
        RenderFlag.SKIP_TOP_LEVEL_ISOLATION
    ).toTypedArray()

    private val noClipFlags = (defaultFlags + RenderFlag.DISABLE_TOP_LEVEL_CLIPPING)

    override fun DrawScope.onDraw() {
        val composition = composition ?: return

        if (composition.animation.isClosed || composition.invalidationController.isClosed)
            return

        drawIntoCanvas {
            val flags = if (clipToCompositionBounds) {
                it.clipRect(0f, 0f, size.width, size.height)
                defaultFlags
            } else {
                noClipFlags
            }


            composition.animation
                .seek(progress, composition.invalidationController)
                .render(
                    canvas = it.nativeCanvas,
                    dst = Rect.makeWH(size.width, size.height),
                    *flags
                )
        }
    }
}

