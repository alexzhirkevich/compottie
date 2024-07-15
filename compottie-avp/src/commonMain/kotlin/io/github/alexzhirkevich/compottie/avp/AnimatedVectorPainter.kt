package io.github.alexzhirkevich.compottie.avp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import org.jetbrains.compose.resources.DefaultComposeEnvironment
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getResourceItemByEnvironment
import kotlin.math.roundToInt



@Composable
public fun rememberAnimatedVectorPainter(
    resource : DrawableResource,
    animations : List<ObjectAnimator<*,*>>,
    isAtAnd : Boolean
) : Painter {
    val environment = LocalComposeEnvironment.cu
    val path =  resource.getResourceItemByEnvironment(environment).path
}


private class AnimatedVectorPainter(
    private val vector: AnimatedImageVector,
    density : Density,
    time : () -> Float
) : Painter() {

    override val intrinsicSize: Size = density.run {
        DpSize(
            vector.defaultWidth,
            vector.defaultHeight
        ).toSize()
    }

    private val viewportSize = IntSize(
        vector.viewportWidth.roundToInt(),
        vector.viewportHeight.roundToInt()
    )

    private val root = AnimatedGroupComponent(vector.root)

    private val time by derivedStateOf { time() }

    override fun DrawScope.onDraw() {
        val scale = ContentScale.FillBounds.computeScaleFactor(intrinsicSize, size)

        val offset = Alignment.Center.align(
            size = viewportSize,
            space = IntSize(
                size.width.roundToInt(),
                size.height.roundToInt()
            ),
            layoutDirection = layoutDirection
        )

        scale(scale.scaleX, scale.scaleY) {
            translate(offset.x.toFloat(), offset.y.toFloat()) {
                root.run {
                    draw(time)
                }
            }
        }
    }
}
