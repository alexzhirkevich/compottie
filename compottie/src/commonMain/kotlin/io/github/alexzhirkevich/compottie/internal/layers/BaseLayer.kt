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
import io.github.alexzhirkevich.compottie.L
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsApplier
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MaskMode
import io.github.alexzhirkevich.compottie.internal.helpers.isInvert
import io.github.alexzhirkevich.compottie.internal.helpers.isLuma
import io.github.alexzhirkevich.compottie.internal.platform.Luma
import io.github.alexzhirkevich.compottie.internal.platform.drawRect
import io.github.alexzhirkevich.compottie.internal.platform.isAndroidAtMost
import io.github.alexzhirkevich.compottie.internal.platform.saveLayer
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.utils.intersectOrReset
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.union

internal abstract class BaseLayer() : Layer {

    override var painterProperties: PainterProperties? = null

    override var namePath: String? = null

    protected val boundsMatrix = Matrix()
    private val path = Path()

    private val matrix = Matrix()
    private val canvasMatrix = Matrix()
    private val canvasBounds = MutableRect(0f, 0f, 0f, 0f)

    private val contentPaint by lazy {
        Paint().apply {
            isAntiAlias = true
        }
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
            isAntiAlias = false
            if (matteMode?.isLuma() == true){
                colorFilter = ColorFilter.Luma
            }
            blendMode = if (matteMode?.isInvert() == true){
                BlendMode.DstOut
            } else BlendMode.DstIn
        }
    }
    private val rect = MutableRect(0f, 0f, 0f, 0f)
    private var parentLayers: MutableList<BaseLayer>? = null

    private var parentLayer: BaseLayer? = null
    private var matteLayer: BaseLayer? = null

    override val effectsApplier by lazy {
        LayerEffectsApplier(this)
    }

    override fun onCreate(composition: LottieComposition) {
        super.onCreate(composition)
        transform.autoOrient = autoOrient == BooleanInt.Yes

        if (name != null) {
            transform.dynamic = composition
                .dynamic?.get(layerPath(namePath, name!!))?.transform
        }
    }

    abstract fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState,
    )

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState,
    ) {
        try {

            if (hidden || (inPoint ?: 0f) > state.frame || (outPoint ?: Float.MAX_VALUE) < state.frame)
                return

            buildParentLayerListIfNeeded()
            matrix.reset()

            matrix.setFrom(parentMatrix)
            parentLayers?.fastForEachReversed {
                matrix.preConcat(it.transform.matrix(state))
            }

            var alpha = 1f

            transform.opacity?.interpolatedNorm(state)?.let {
                alpha = (alpha * it).coerceIn(0f, 1f)
            }

            alpha = transform.dynamic?.opacity.derive(alpha, state)

            alpha = (alpha * parentAlpha.coerceIn(0f,1f))

            if (matteLayer == null && !hasMask()) {
                matrix.preConcat(transform.matrix(state))
                drawLayer(drawScope, matrix, alpha, state)
                return
            }

            getBounds(drawScope, matrix, false, state, rect)

            intersectBoundsWithMatte(drawScope, rect, matrix, state)

            matrix.preConcat(transform.matrix(state))
            intersectBoundsWithMask(rect, matrix, state)

            // Intersect the mask and matte rect with the canvas bounds.
            // If the canvas has a transform, then we need to transform its bounds by its matrix
            // so that we know the coordinate space that the canvas is showing.
            canvasBounds.set(0f, 0f, drawScope.size.width, drawScope.size.height)
            drawScope.drawIntoCanvas { canvas ->

//                canvas.getMatrix(canvasMatrix)
//                if (!canvasMatrix.isIdentity()) {
//                    canvasMatrix.invert()
//                    canvasMatrix.map(canvasBounds)
//                }

                rect.intersectOrReset(canvasBounds)

                // Ensure that what we are drawing is >=1px of width and height.
                // On older devices, drawing to an offscreen buffer of <1px would draw back as a black bar.
                // https://github.com/airbnb/lottie-android/issues/1625
                if (rect.width >= 1f && rect.height >= 1f) {
                    contentPaint.alpha = 1f
                    canvas.saveLayer(rect, contentPaint)

                    // Clear the off screen buffer. This is necessary for some phones.
                    clearCanvas(canvas)
                    drawLayer(drawScope, matrix, alpha, state)

                    if (hasMask()) {
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

//            if (outlineMasksAndMattes && outlineMasksAndMattesPaint != null) {
//                outlineMasksAndMattesPaint.setStyle(android.graphics.Paint.Style.STROKE)
//                outlineMasksAndMattesPaint.setColor(-0x3d7fd)
//                outlineMasksAndMattesPaint.setStrokeWidth(4f)
//                canvas.drawRect(rect, outlineMasksAndMattesPaint)
//                outlineMasksAndMattesPaint.setStyle(android.graphics.Paint.Style.FILL)
//                outlineMasksAndMattesPaint.setColor(0x50EBEBEB)
//                canvas.drawRect(rect, outlineMasksAndMattesPaint)
//            }
            }
        } catch (t: Throwable) {
            L.logger.error("Lottie crashed in draw :(", t)
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
        boundsMatrix.setFrom(parentMatrix)

        if (applyParents) {
            val p = parentLayers
            if (p != null){
                p.fastForEachReversed {
                    boundsMatrix.preConcat(it.transform.matrix(state))
                }
            } else {
                parentLayer?.transform?.matrix(state)?.let {
                    boundsMatrix.preConcat(it)
                }
            }
        }

        boundsMatrix.preConcat(transform.matrix(state))
    }


    final override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
    }

    fun setParentLayer(layer: BaseLayer) {
        this.parentLayer = layer
    }

    fun setMatteLayer(layer: BaseLayer) {
        this.matteLayer = layer
    }

    private fun buildParentLayerListIfNeeded() {
        if (parentLayers != null) {
            return
        }
        if (parentLayer == null) {
            parentLayers = mutableListOf()
            return
        }

        parentLayers = mutableListOf()
        var layer: BaseLayer? = parentLayer
        while (layer != null) {
            parentLayers?.add(layer)
            layer = layer.parentLayer
        }
    }

    private fun hasMask(): Boolean = !masks.isNullOrEmpty()

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

        if (!hasMask()) {
            return
        }

        masks?.fastForEach { mask ->

            val maskPath = mask.shape?.interpolatedRaw(state) ?: return@fastForEach
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
                    val b = path.getBounds()
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
                MaskMode.None ->           // None mask should have no effect. If all masks are NONE, fill the
                    // mask canvas with a rectangle so it fully covers the original layer content.
                    // However, if there are other masks, they should be the only ones that have an effect so
                    // this should noop.
                    if (this.masks!!.all { it.mode == MaskMode.None }) {
                        contentPaint.alpha = 1f
                        canvas.drawRect(rect, contentPaint)
                    }

                MaskMode.Add -> if (mask.isInverted) {
                    applyInvertedAddMask(canvas, matrix, mask, state)
                } else {
                    applyAddMask(canvas, matrix, mask, state)
                }

                MaskMode.Subtract -> {
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
        val maskPath = mask.shape?.interpolated(state) ?: return
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
        val maskPath = mask.shape?.interpolatedRaw(state) ?: return
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
        val maskPath = mask.shape?.interpolated(state) ?: return
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
        val maskPath = mask.shape?.interpolated(state) ?: return
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
        val maskPath = mask.shape?.interpolated(state) ?: return
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
        val maskPath = mask.shape?.interpolated(state) ?: return
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

