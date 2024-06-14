package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.isSupported
import io.github.alexzhirkevich.compottie.internal.platform.clipRect
import io.github.alexzhirkevich.compottie.internal.platform.saveLayer
import io.github.alexzhirkevich.compottie.internal.utils.union
import kotlinx.serialization.Transient

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

    override fun onCreate(composition: LottieComposition) {
        layers.forEach { it.onCreate(composition) }
    }

//    private val remappedState  by lazy {
//        RemappedAnimationState(
//            frameRemapping = ::remappedFrame
//        )
//    }

    abstract fun loadLayers(): List<Layer>

    private val layers by lazy {
        val layers = loadLayers().filterIsInstance<BaseLayer>()

        if (name != null) {
            layers.fastForEach {
                it.namePath = layerPath(this.namePath, name!!)
            }
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

        (layers - matteLayers).filterNot { it.matteTarget == BooleanInt.Yes }
    }

    override var painterProperties: PainterProperties?
        get() = super.painterProperties
        set(value) {
            super.painterProperties = value
            layers.fastForEach {
                it.painterProperties = value
            }
        }

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {
//        remappedState.delegate = state

        newClipRect.set(0f, 0f, width, height)
        parentMatrix.map(newClipRect)

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isDrawingWithOffScreen = layers.isEmpty() && parentAlpha < 1f

        val canvas = drawScope.drawContext.canvas

        if (isDrawingWithOffScreen) {
            layerPaint.alpha = parentAlpha
            canvas.saveLayer(newClipRect, layerPaint)
        } else {
            canvas.save()
        }

        val childAlpha = if (isDrawingWithOffScreen) 1f else parentAlpha

        state.remapped(getRemappedFrame(state)) { remappedState ->
            layers.fastForEachReversed { layer ->
                // Only clip precomps. This mimics the way After Effects renders animations.
                val ignoreClipOnThisLayer =
                    isContainerLayer || painterProperties?.clipToDrawBounds == false

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

        layers.fastForEachReversed {
            rect.set(0f, 0f, 0f, 0f)
            it.getBounds(drawScope, boundsMatrix, true, state, rect)
            outBounds.union(rect)
        }
    }

    private fun getRemappedFrame(state: AnimationState): Float {

        val f = if (timeStretch != 0f && timeStretch != 1f && !isContainerLayer) {
            state.frame / timeStretch
        } else state.frame

        val tr = timeRemapping ?: return f

        val composition = state.composition

        return state.remapped(f) {
            tr.interpolated(it) * composition.frameRate - composition.startFrame
        }
    }
}