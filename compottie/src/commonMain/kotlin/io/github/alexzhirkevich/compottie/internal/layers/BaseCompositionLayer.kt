package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.isSupported
import io.github.alexzhirkevich.compottie.internal.platform.clipRect
import io.github.alexzhirkevich.compottie.internal.platform.saveLayer
import io.github.alexzhirkevich.compottie.internal.utils.union
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.serialization.Transient
import kotlin.math.absoluteValue

internal abstract class BaseCompositionLayer: BaseLayer() {

    abstract val width: Float

    abstract val height: Float

    abstract val timeRemapping: AnimatedNumber?

    private val rect = MutableRect(0f, 0f, 0f, 0f)

    @Transient
    private val newClipRect = MutableRect(0f, 0f, 0f, 0f)

    @Transient
    private val layerPaint = Paint().apply {
        isAntiAlias = true
    }

    private val getLayerLock = SynchronizedObject()

    private var loadedLayers: List<BaseLayer>? = null

    abstract fun compose(state: AnimationState): List<Layer>

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {

        val layers = getLayers(state)

        newClipRect.set(0f, 0f, width, height)
        parentMatrix.map(newClipRect)

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isDrawingWithOffScreen = state.applyOpacityToLayers &&
                layers.isNotEmpty() && parentAlpha < .99f

        val canvas = drawScope.drawContext.canvas

        if (isDrawingWithOffScreen) {
            layerPaint.alpha = parentAlpha
            canvas.saveLayer(newClipRect, layerPaint)
        } else {
            canvas.save()
        }

        val childAlpha = if (isDrawingWithOffScreen) 1f else parentAlpha

        state.onFrame(getRemappedFrame(state)) { remappedState ->

            layers.fastForEachReversed { layer ->
                // Only clip precomps. This mimics the way After Effects renders animations.
                val ignoreClipOnThisLayer =
                    isContainerLayer || !state.clipToCompositionBounds

                if (!ignoreClipOnThisLayer && !newClipRect.isEmpty) {
                    canvas.clipRect(newClipRect)
                }


                layer.draw(drawScope, parentMatrix, childAlpha, remappedState)
            }
        }

        canvas.restore()
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {
        super.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)

        getLayers(state).fastForEachReversed {
            rect.set(0f, 0f, 0f, 0f)
            it.getBounds(drawScope, boundsMatrix, true, state, rect)
            outBounds.union(rect)
        }
    }

    override fun setDynamicProperties(
        composition: DynamicCompositionProvider,
        state: AnimationState
    ): DynamicLayerProvider? {
        val dynamic = super.setDynamicProperties(composition, state)

        getLayers(state).fastForEach {
            it.setDynamicProperties(composition, state)
        }

        return dynamic
    }

    private fun getLayers(state: AnimationState): List<Layer> = synchronized(getLayerLock) {
        loadedLayers?.let { return@synchronized it }

        val layers = compose(state).filterIsInstance<BaseLayer>()

        layers.fastForEach {
            it.resolvingPath = this.resolvingPath?.resolveOrNull(it.name)
        }

        layers.fastForEach {
            it.effects = effects + it.effects
        }

        val matteLayers = mutableSetOf<BaseLayer>()

        val layersWithIndex = layers
            .filter { it.index != null }
            .associateBy { it.index }

        layers.forEachIndexed { i, it ->
            it.parent?.let { pId ->
                val p = layersWithIndex[pId]

                if (p != null) {
                    it.setParentLayer(p)
                }
            }

            if (it.matteMode?.isSupported() == true) {
                if (it.matteParent != null) {
                    val p = layersWithIndex[it.matteParent]

                    if (p != null) {
                        it.setMatteLayer(p)
                        matteLayers.add(p)
                    }
                } else {
                    if (i > 0) {
                        it.setMatteLayer(layers[i - 1])
                        matteLayers.add(layers[i - 1])
                    }
                }

            }
        }

        this.loadedLayers = (layers - matteLayers).fastFilter { it.matteTarget != BooleanInt.Yes }
        return this.loadedLayers!!
    }
    private fun getRemappedFrame(state: AnimationState): Float {

        val frame = timeRemapping?.interpolated(state)
            ?.times(state.composition.frameRate)
            ?.minus(state.composition.startFrame)
            ?: (state.frame - (startTime ?: inPoint ?: 0f)  )

        return if (timeStretch.absoluteValue > Float.MIN_VALUE && timeStretch != 1f && !isContainerLayer) {
            frame / timeStretch
        } else frame
    }
}