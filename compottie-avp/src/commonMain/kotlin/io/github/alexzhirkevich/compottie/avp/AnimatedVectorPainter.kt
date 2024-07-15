@file: Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.avp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import io.github.alexzhirkevich.compottie.avp.xml.toAnimatedImageVector
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.getResourceItemByEnvironment
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.LocalResourceReader
import org.jetbrains.compose.resources.toXmlElement
import kotlin.math.roundToInt

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
public fun rememberAnimatedVectorPainter(
    resource : DrawableResource,
    animations : List<ObjectAnimator<*,*>>,
    isAtAnd : Boolean
) : Painter {
    val environment = LocalComposeEnvironment.current.rememberEnvironment()
    val path =  resource.getResourceItemByEnvironment(environment).path

    val density = LocalDensity.current
    val resourceReader = LocalResourceReader.current

    val painter by produceState<Painter>(EmptyPainter, density) {
        val image = resourceReader.read(path).toXmlElement()
        val vector = image.toAnimatedImageVector(density, emptyMap())
        value = AnimatedVectorPainter(
            vector,
            density,
            { 0f }
        )
    }
    return painter
}

private object EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }

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
