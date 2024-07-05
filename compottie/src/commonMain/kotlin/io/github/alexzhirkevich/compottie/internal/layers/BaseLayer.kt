package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsApplier
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MaskMode
import io.github.alexzhirkevich.compottie.internal.helpers.isInvert
import io.github.alexzhirkevich.compottie.internal.helpers.isLuma
import io.github.alexzhirkevich.compottie.internal.platform.Luma
import io.github.alexzhirkevich.compottie.internal.platform.drawRect
import io.github.alexzhirkevich.compottie.internal.platform.isAndroidAtMost
import io.github.alexzhirkevich.compottie.internal.platform.saveLayer
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.utils.fastSetFrom
import io.github.alexzhirkevich.compottie.internal.utils.intersectOrReset
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.union

internal abstract class BaseLayer : Layer {

    override var resolvingPath : ResolvingPath? = null

    protected val boundsMatrix = Matrix()

    private val path = Path()

    private val matrix = Matrix()
    private val canvasMatrix = Matrix()
    private val canvasBounds = MutableRect(0f, 0f, 0f, 0f)

    private val contentPaint = Paint().apply {
        isAntiAlias = true
    }

    private val clearPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            blendMode = BlendMode.Clear
        }
    }

    private val dstInPaint by lazy {
        Paint().apply {
            blendMode = BlendMode.DstIn
            isAntiAlias = true
        }
    }

    private val dstOutPaint by lazy {
        Paint().apply {
            blendMode = BlendMode.DstOut
            isAntiAlias = true
        }
    }

    private val maskBoundsRect = MutableRect(0f, 0f, 0f, 0f)
    private val matteBoundsRect = MutableRect(0f, 0f, 0f, 0f)
    private val mattePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            if (matteMode?.isLuma() == true){
                colorFilter = ColorFilter.Luma
            }
            blendMode = if (matteMode?.isInvert() == true){
                BlendMode.DstOut
            } else BlendMode.DstIn
        }
    }
    private val rect = MutableRect(0f, 0f, 0f, 0f)
    private var parentLayers: MutableList<Layer>? = null

    override var parentLayer: Layer? = null

    private var matteLayer: BaseLayer? = null

    private val allMasksAreNone by lazy {
        masks?.all { it.mode == MaskMode.None } == true
    }

    override val effectsApplier by lazy {
        LayerEffectsApplier(this)
    }

    protected var dynamicLayer : DynamicLayerProvider? = null
        private set

    override fun setDynamicProperties(
        composition: DynamicCompositionProvider?,
        state: AnimationState
    ) : DynamicLayerProvider? {
        dynamicLayer = resolvingPath?.let {
            composition?.get(it)
        }
        transform.dynamic = dynamicLayer?.transform
        return dynamicLayer
    }

    abstract fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState,
    )

    final override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState,
    ) {
        try {
            state.onLayer(this) {

                if (dynamicLayer?.hidden.derive(hidden, state)
                    || (inPoint ?: Float.MIN_VALUE) > state.frame
                    || (outPoint ?: Float.MAX_VALUE) < state.frame
                ) return@onLayer

                buildParentLayerListIfNeeded()

                matrix.fastSetFrom(parentMatrix)
                parentLayers?.fastForEachReversed {
                    matrix.preConcat(it.transform.matrix(state))
                }

                var alpha = 1f

                transform.opacity.interpolatedNorm(state).let {
                    alpha = (alpha * it).coerceIn(0f, 1f)
                }

                alpha = (alpha * parentAlpha.coerceIn(0f, 1f))

                if (matteLayer == null && !hasMasks()) {
                    matrix.preConcat(transform.matrix(state))
                    drawLayer(drawScope, matrix, alpha, state)
                    return@onLayer
                }

                rect.set(0f,0f,0f,0f)
                getBounds(drawScope, matrix, false, state, rect)

                intersectBoundsWithMatte(drawScope, rect, parentMatrix, state)

                matrix.preConcat(transform.matrix(state))
                intersectBoundsWithMask(rect, matrix, state)

                // Intersect the mask and matte rect with the canvas bounds.
                // If the canvas has a transform, then we need to transform its bounds by its matrix
                // so that we know the coordinate space that the canvas is showing.
//                canvasBounds.set(0f, 0f, drawScope.size.width, drawScope.size.height)
                drawScope.drawIntoCanvas { canvas ->

//                    canvas.getMatrix(canvasMatrix)
//                    if (!canvasMatrix.isIdentity()) {
//                        canvasMatrix.invert()
//                        canvasMatrix.map(canvasBounds)
//                    }

//                    rect.intersectOrReset(canvasBounds)
//
                    // Ensure that what we are drawing is >=1px of width and height.
                    // On older devices, drawing to an offscreen buffer of <1px would draw back as a black bar.
                    // https://github.com/airbnb/lottie-android/issues/1625
                    if (rect.width >= 1f && rect.height >= 1f) {
                        contentPaint.alpha = 1f
                        canvas.saveLayer(rect, contentPaint)

                        // Clear the off screen buffer. This is necessary for some phones.
                        clearCanvas(canvas)
                        drawLayer(drawScope, matrix, alpha, state)

                        if (hasMasks()) {
                            applyMasks(canvas, matrix, state)
                        }

                        matteLayer?.let {
                            canvas.saveLayer(rect, mattePaint, SAVE_FLAGS)
                            clearCanvas(canvas)
                            it.draw(drawScope, parentMatrix, alpha, state)
                            canvas.restore()
                        }

                        canvas.restore()
                    }

//                        val outlineMasksAndMattesPaint = Paint().apply {
//                            style = PaintingStyle.Stroke
//                            strokeWidth = 4f
//                            color = Color.Red
//                        }
//                        canvas.drawRect(rect, outlineMasksAndMattesPaint)
//                        outlineMasksAndMattesPaint.style = PaintingStyle.Fill
//                        outlineMasksAndMattesPaint.color = Color(0x50EBEBEB)
//                        canvas.drawRect(rect, outlineMasksAndMattesPaint)
                }
            }
        } catch (t: Throwable) {
            Compottie.logger?.error("Lottie crashed in draw :(", t)
        }
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect,
    ) {
        rect.set(0f, 0f, 0f, 0f)
        buildParentLayerListIfNeeded()
        boundsMatrix.fastSetFrom(parentMatrix)

        if (applyParents) {
            val p = parentLayers
            if (p != null){
                p.fastForEachReversed {
                    boundsMatrix.preConcat(it.transform.matrix(state))
                }
            } else {
                this.parentLayer?.transform?.matrix(state)?.let {
                    boundsMatrix.preConcat(it)
                }
            }
        }

        boundsMatrix.preConcat(transform.matrix(state))
    }


    final override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
    }


    fun setMatteLayer(layer: BaseLayer) {
        this.matteLayer = layer
    }

    private fun buildParentLayerListIfNeeded() {
        if (parentLayers != null) {
            return
        }
        if (this.parentLayer == null) {
            parentLayers = mutableListOf()
            return
        }

        parentLayers = mutableListOf()
        var layer = this.parentLayer
        while (layer != null) {
            parentLayers?.add(layer)
            layer = layer.parentLayer
        }
    }

    private fun hasMasks(): Boolean = hasMask != false && !masks.isNullOrEmpty()

    private fun clearCanvas(canvas: Canvas) {
        // If we don't pad the clear draw, some phones leave a 1px border of the graphics buffer.
        canvas.drawRect(
            rect.left - 1,
            rect.top - 1,
            rect.right + 1,
            rect.bottom + 1,
            clearPaint
        )
    }

    private fun intersectBoundsWithMask(rect: MutableRect, matrix: Matrix, state: AnimationState) {

        maskBoundsRect.set(0f, 0f, 0f, 0f)

        if (!hasMasks()) {
            return
        }

        masks?.fastForEach { mask ->
            val maskPath = mask.shape?.interpolatedMutable(state) ?: return@fastForEach
            path.set(maskPath)
            path.transform(matrix)

            when (mask.mode) {
                MaskMode.None,
                MaskMode.Subtract ->
                    // If there is a subtract mask, the mask could potentially be the size of the entire
                    // canvas so we can't use the mask bounds.
                    return

                else -> {
                    if (mask.isInverted) {
                        return
                    }
                    maskBoundsRect.union(path.getBounds())
                }
            }
        }

        rect.intersectOrReset(maskBoundsRect)
    }

    private fun intersectBoundsWithMatte(
        drawScope: DrawScope,
        rect: MutableRect,
        matrix: Matrix,
        state: AnimationState
    ) {

        val matteLayer = matteLayer ?: return

        if (matteMode?.isInvert() == true) {
            // We can't trim the bounds if the mask is inverted since it extends all the way to the
            // composition bounds.
            return
        }
        matteBoundsRect.set(0f, 0f, 0f, 0f)
        matteLayer.getBounds(drawScope, matrix, true, state, matteBoundsRect)

        rect.intersectOrReset(matteBoundsRect)
    }

    private fun applyMasks(canvas: Canvas, matrix: Matrix, state: AnimationState) {
        canvas.saveLayer(rect, dstInPaint, SAVE_FLAGS)

        if (isAndroidAtMost(27)) {
            clearCanvas(canvas)
        }

        masks?.fastForEachIndexed { i, mask ->

            when (mask.mode) {
                MaskMode.None ->
                    // None mask should have no effect. If all masks are NONE, fill the
                    // mask canvas with a rectangle so it fully covers the original layer content.
                    // However, if there are other masks, they should be the only ones that have an effect so
                    // this should noop.
                    if (allMasksAreNone) {
                        contentPaint.alpha = 1f
                        canvas.drawRect(rect, contentPaint)
                    }

                MaskMode.Subtract-> {
                    if (i == 0) {
                        contentPaint.color = Color.Black
                        contentPaint.alpha = 1f
                        canvas.drawRect(rect, contentPaint)
                    }
                    if (mask.isInverted) {
                        applyInvertedSubtractMask(canvas, matrix, mask, state)
                    } else {
                        applySubtractMask(canvas, matrix, mask, state)
                    }
                }

                MaskMode.Intersect -> if (mask.isInverted) {
                    applyInvertedIntersectMask(canvas, matrix, mask, state)
                } else {
                    applyIntersectMask(canvas, matrix, mask, state)
                }

                // MaskMode.Add
                else -> if (mask.isInverted) {
                    applyInvertedAddMask(canvas, matrix, mask, state)
                } else {
                    applyAddMask(canvas, matrix, mask, state)
                }

            }
        }
        canvas.restore()
    }

    private fun applyInvertedAddMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        canvas.saveLayer(rect, contentPaint)
        canvas.drawRect(rect, contentPaint)
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolatedNorm(state)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, dstOutPaint)
        canvas.restore()
    }

    private fun applyAddMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolatedNorm(state)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, contentPaint)
    }

    private fun applySubtractMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        canvas.drawPath(path, dstOutPaint)
    }

    private fun applyInvertedSubtractMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        canvas.saveLayer(rect, dstOutPaint)
        canvas.drawRect(rect, contentPaint)
        dstOutPaint.alpha = mask.opacity?.interpolatedNorm(state)
            ?.coerceIn(0f, 1f) ?: 1f
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        canvas.drawPath(path, dstOutPaint)
        canvas.restore()
    }

    private fun applyIntersectMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        canvas.saveLayer(rect, dstInPaint)
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolatedNorm(state)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, contentPaint)
        canvas.restore()
    }

    private fun applyInvertedIntersectMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        state: AnimationState,
    ) {
        canvas.saveLayer(rect, dstInPaint)
        canvas.drawRect(rect, contentPaint)
        dstOutPaint.alpha = mask.opacity?.interpolatedNorm(state)?.coerceIn(0f, 1f) ?: 1f
        val maskPath = mask.shape?.interpolatedMutable(state) ?: return
        path.set(maskPath)
        path.transform(matrix)
        canvas.drawPath(path, dstOutPaint)
        canvas.restore()
    }

}

private const val CLIP_SAVE_FLAG = 0x02
private const val CLIP_TO_LAYER_SAVE_FLAG = 0x10
private const val MATRIX_SAVE_FLAG = 0x01
private const val SAVE_FLAGS = CLIP_SAVE_FLAG or CLIP_TO_LAYER_SAVE_FLAG or MATRIX_SAVE_FLAG

