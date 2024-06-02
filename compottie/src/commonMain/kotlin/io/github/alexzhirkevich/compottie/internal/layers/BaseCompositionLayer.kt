package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.platform.clipRect
import io.github.alexzhirkevich.compottie.internal.utils.Utils
import io.github.alexzhirkevich.compottie.internal.utils.union
import kotlinx.serialization.Transient

internal abstract class BaseCompositionLayer: BaseLayer() {

    abstract val width : Float

    abstract val height : Float

    abstract val timeRemapping : AnimatedValue?

    @Transient
    private val newClipRect = MutableRect(0f, 0f, 0f, 0f)

    @Transient
    private val layerPaint = Paint()

    abstract fun loadLayers() : List<Layer>

    private val layers by lazy {
        val l = loadLayers().filterIsInstance<BaseLayer>()

        val layersWithIndex = l
            .filter { it.index != null }
            .associateBy { it.index }

        l.forEach {
            it.parent?.let { pId ->
                val p = layersWithIndex[pId]

                if (p != null){
                    it.setParentLayer(p)
                }
            }
        }
        l
    }

    override var density: Float
        get() = super.density
        set(value) {
            super.density = value
            layers.fastForEach { it.density = value }
        }

    override var maintainOriginalImageBounds: Boolean
        get() = super.maintainOriginalImageBounds
        set(value) {
            super.maintainOriginalImageBounds = value
            layers.fastForEach {
                it.maintainOriginalImageBounds = value
            }
        }

    override var assets: Map<String, LottieAsset>
        get() = super.assets
        set(value) {
            super.assets = value
            layers.fastForEach { it.assets = value }
        }

    final override var composition: LottieComposition
        get() = super.composition
        set(value) {
            super.composition = value
            layers.fastForEach {
                it.composition = value
            }
        }

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float
    ) {

        newClipRect.set(0f, 0f, width, height)
        parentMatrix.map(newClipRect)

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isDrawingWithOffScreen = layers.isEmpty() && parentAlpha < 1f

        drawScope.drawIntoCanvas { canvas ->

            if (isDrawingWithOffScreen) {
                layerPaint.alpha = parentAlpha
                Utils.saveLayerCompat(canvas, newClipRect, layerPaint)
            } else {
                canvas.save()
            }

            val childAlpha = if (isDrawingWithOffScreen) 1f else parentAlpha

            layers.fastForEachReversed { layer ->
                // Only clip precomps. This mimics the way After Effects renders animations.
                val ignoreClipOnThisLayer = isContainerLayer

                if (!ignoreClipOnThisLayer && !newClipRect.isEmpty) {
                    canvas.clipRect(newClipRect)
                }

                layer.draw(
                    drawScope,
                    parentMatrix,
                    childAlpha,
                    remappedFrame(frame)
                )
            }

            canvas.restore()
        }
    }

    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float
    ) {
        super.getBounds(outBounds, parentMatrix, applyParents, frame)
        layers.fastForEachReversed {
            rect.set(0f, 0f, 0f, 0f)
            it.getBounds(rect, boundsMatrix, true,frame)
            outBounds.union(rect)
        }
    }

    private fun remappedFrame(frame: Float): Float {

        val tr = timeRemapping ?: return frame

        val f = if (timeStretch != 0f && !isContainerLayer) {
            frame / timeStretch
        } else frame

        return tr.interpolated(f) *
                composition.frameRate - composition.startFrame
    }
}