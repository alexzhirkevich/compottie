@file: Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.avp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import io.github.alexzhirkevich.compottie.avp.xml.drawable
import io.github.alexzhirkevich.compottie.avp.xml.parseAnimationTargets
import io.github.alexzhirkevich.compottie.avp.xml.parseObjectAnimators
import io.github.alexzhirkevich.compottie.avp.xml.toAnimatedImageVector
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    isAtAnd : Boolean
) : Painter {

    val environment = LocalComposeEnvironment.current.rememberEnvironment()
    val path = resource.getResourceItemByEnvironment(environment).path

    val density = LocalDensity.current
    val resourceReader = LocalResourceReader.current

    val vector = produceState<AnimatedImageVector?>(null, density) {
        value = loadXmlVector(density, path, resourceReader::read)
    }
    
    return remember(vector, density) {
        AnimatedVectorPainter(
            vector,
            density,
            { 0f }
        )
    }
}

private object EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size(1f,1f)

    override fun DrawScope.onDraw() {
    }

}


private class AnimatedVectorPainter(
    private val vector: State<AnimatedImageVector?>,
    density : Density,
    time : () -> Float
) : Painter() {

    override val intrinsicSize: Size by derivedStateOf {
        density.run {
            DpSize(
                vector.value?.defaultWidth ?: 1.dp,
                vector.value?.defaultHeight ?: 1.dp,
            ).toSize()
        }
    }

    private val viewportSize by derivedStateOf {
        IntSize(
            vector.value?.viewportWidth?.roundToInt() ?: 1,
            vector.value?.viewportHeight?.roundToInt() ?: 1
        )
    }

    private val root by derivedStateOf {
        vector.value?.root?.let(::AnimatedGroupComponent)
    }

    private val time by derivedStateOf { time() }

    override fun DrawScope.onDraw() {
        root?.run {
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
                    draw(time)
                }
            }
        }
    }
}

private suspend fun loadXmlVector(
    density: Density,
    resource : String,
    readBytes : suspend (String) -> ByteArray,
) : AnimatedImageVector {

    val xml = readBytes(resource).toXmlElement()
    val drawable = readBytes(xml.drawable())

    val animators = coroutineScope {
        xml.parseAnimationTargets()
            .map {
                async {
                    it.name to readBytes(it.animation)
                        .toXmlElement()
                        .parseObjectAnimators()
                        .associateBy(ObjectAnimator<*,*>::property)
                }
            }
            .awaitAll()
            .toMap()
    }

    return drawable.toXmlElement().toAnimatedImageVector(
        density = density,
        animators = animators
    )
}
