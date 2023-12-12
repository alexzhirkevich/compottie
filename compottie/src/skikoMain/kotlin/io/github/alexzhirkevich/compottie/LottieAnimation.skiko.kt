package io.github.alexzhirkevich.compottie

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController




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

    Canvas(
        modifier
            .size(defaultSize)
    ) {
        drawIntoCanvas {
            if (composition != null) {
                val currentProgress = progress()
                composition.animation.seek(currentProgress, composition.invalidationController)

                composition.animation.render(
                    canvas = it.nativeCanvas,
                    dst = Rect.makeWH(size.width, size.height)
                )
            }
        }
    }
}
