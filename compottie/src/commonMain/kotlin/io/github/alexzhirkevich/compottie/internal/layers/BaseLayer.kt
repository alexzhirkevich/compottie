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
import androidx.compose.ui.graphics.isIdentity
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.effects.BlurEffect
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MaskMode
import io.github.alexzhirkevich.compottie.internal.helpers.isInvert
import io.github.alexzhirkevich.compottie.internal.helpers.isLuma
import io.github.alexzhirkevich.compottie.internal.platform.Luma
import io.github.alexzhirkevich.compottie.internal.platform.drawRect
import io.github.alexzhirkevich.compottie.internal.platform.getMatrix
import io.github.alexzhirkevich.compottie.internal.platform.isAndroidAtMost
import io.github.alexzhirkevich.compottie.internal.platform.saveLayer
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.platform.setBlurMaskFilter
import io.github.alexzhirkevich.compottie.internal.utils.intersectOrReset
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlin.math.max
import kotlin.math.min

internal abstract class BaseLayer() : Layer, DrawingContent {

    override var painterProperties: PainterProperties? = null

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
            isAntiAlias = true
            if (matteMode?.isLuma() == true){
                colorFilter = ColorFilter.Luma
            }
            blendMode = if (matteMode?.isInvert() == true){
                BlendMode.DstOut
            } else BlendMode.DstIn
        }
    }
    protected val rect = MutableRect(0f, 0f, 0f, 0f)
    private var parentLayers: MutableList<BaseLayer>? = null

    private var parentLayer: BaseLayer? = null
    private var matteLayer: BaseLayer? = null

    private val blurEffect by lazy {
        effects.fastFirstOrNull { it is BlurEffect } as? BlurEffect
    }

    abstract fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float,
    )

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float,
    ) {

        if (hidden || (inPoint ?: 0f) > frame || (outPoint ?: Float.MAX_VALUE) < frame)
            return

        buildParentLayerListIfNeeded()
        matrix.reset()

        matrix.setFrom(parentMatrix)
        parentLayers?.fastForEachReversed {
            matrix.preConcat(it.transform.matrix(frame))
        }

        var alpha = parentAlpha

        transform.opacity?.interpolated(frame)?.let {
            alpha = (alpha * (it / 100f)).coerceIn(0f, 1f)
        }

        if (matteLayer == null && !hasMask()) {
            matrix.preConcat(transform.matrix(frame))
            drawLayer(drawScope, matrix, alpha, frame)
            return
        }

        getBounds(drawScope, matrix, false, frame, rect)

        intersectBoundsWithMatte(drawScope, rect, matrix, frame)

        matrix.preConcat(transform.matrix(frame))
        intersectBoundsWithMask(rect, matrix, frame)

        // Intersect the mask and matte rect with the canvas bounds.
        // If the canvas has a transform, then we need to transform its bounds by its matrix
        // so that we know the coordinate space that the canvas is showing.
        canvasBounds.set(0f, 0f, drawScope.size.width, drawScope.size.height)
        drawScope.drawIntoCanvas { canvas ->
            canvas.getMatrix(canvasMatrix)

            //TODO: fix mask canvas mapping
            if (!canvasMatrix.isIdentity()) {
                canvasMatrix.invert()
                canvasMatrix.map(canvasBounds)
            }

            rect.intersectOrReset(canvasBounds)

            // Ensure that what we are drawing is >=1px of width and height.
            // On older devices, drawing to an offscreen buffer of <1px would draw back as a black bar.
            // https://github.com/airbnb/lottie-android/issues/1625
            if (rect.width >= 1f && rect.height >= 1f) {
                contentPaint.alpha = 1f
                canvas.saveLayer(rect, contentPaint)

                // Clear the off screen buffer. This is necessary for some phones.
                clearCanvas(canvas)
                drawLayer(drawScope, matrix, alpha, frame)

                if (hasMask()) {
                    applyMasks(canvas, matrix, frame)
                }

                matteLayer?.let {
                    canvas.saveLayer(rect, mattePaint, SAVE_FLAGS)
                    clearCanvas(canvas)
                    it.draw(drawScope, parentMatrix, alpha, frame)
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
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
        outBounds: MutableRect,
    ) {
        rect.set(0f, 0f, 0f, 0f)
        buildParentLayerListIfNeeded()
        boundsMatrix.setFrom(parentMatrix)

        if (applyParents) {
            parentLayers?.fastForEachReversed {
                boundsMatrix.preConcat(it.transform.matrix(frame))
            } ?: run {
                parentLayer?.transform?.matrix(frame)?.let {
                    boundsMatrix.preConcat(it)
                }
            }
        }

        boundsMatrix.preConcat(transform.matrix(frame))
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        //do nothing
    }

    fun setParentLayer(layer: BaseLayer) {
        this.parentLayer = layer
    }

    fun setMatteLayer(layer: BaseLayer) {
        this.matteLayer = layer
    }

    override fun applyBlurEffectIfNeeded(paint: Paint, frame: Float,  lastBlurRadius : Float?) : Float {

        return blurEffect?.let {
            val radius = it.radius?.interpolated(frame) ?: return@let null

            if (radius != lastBlurRadius) {
                paint.setBlurMaskFilter(radius, isImage = this is ImageLayer)
            }
            return radius
        } ?: 0f
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

    private fun intersectBoundsWithMask(rect: MutableRect, matrix: Matrix, frame: Float) {
        maskBoundsRect.set(0f, 0f, 0f, 0f)

        if (!hasMask()) {
            return
        }

        masks?.fastForEachIndexed { i, mask ->

            val maskPath = mask.shape?.interpolated(frame) ?: return@fastForEachIndexed

            path.set(maskPath)
            path.transform(matrix)

            when (mask.mode) {
                MaskMode.None,
                MaskMode.Subtract ->
                    // If there is a subtract mask, the mask could potentially be the size of the entire
                    // canvas so we can't use the mask bounds.
                    return

                MaskMode.Intersect,
                MaskMode.Add -> {
                    if (mask.isInverted) {
                        return
                    }
                    val tempMaskBoundsRect = path.getBounds()
                    // As we iterate through the masks, we want to calculate the union region of the masks.
                    // We initialize the rect with the first mask. If we don't call set() on the first call,
                    // the rect will always extend to (0,0).
                    if (i == 0) {
                        maskBoundsRect.set(tempMaskBoundsRect)
                    } else {
                        maskBoundsRect.set(
                            min(maskBoundsRect.left, tempMaskBoundsRect.left),
                            min(maskBoundsRect.top, tempMaskBoundsRect.top),
                            max(maskBoundsRect.right, tempMaskBoundsRect.right),
                            max(maskBoundsRect.bottom, tempMaskBoundsRect.bottom)
                        )
                    }
                }

                else -> {
                    val tempMaskBoundsRect = path.getBounds()
                    if (i == 0) {
                        maskBoundsRect.set(tempMaskBoundsRect)
                    } else {
                        maskBoundsRect.set(
                            min(maskBoundsRect.left, tempMaskBoundsRect.left),
                            min(maskBoundsRect.top, tempMaskBoundsRect.top),
                            max(maskBoundsRect.right, tempMaskBoundsRect.right),
                            max(maskBoundsRect.bottom, tempMaskBoundsRect.bottom)
                        )
                    }
                }
            }
        }

        rect.intersectOrReset(maskBoundsRect)
    }

    private fun intersectBoundsWithMatte(drawScope: DrawScope, rect: MutableRect, matrix: Matrix, frame : Float) {

        val matteLayer = matteLayer ?: return

        if (matteMode?.isInvert() == true) {
            // We can't trim the bounds if the mask is inverted since it extends all the way to the
            // composition bounds.
            return
        }
        matteBoundsRect.set(0f, 0f, 0f, 0f)
        matteLayer.getBounds(drawScope, matrix, true, frame, matteBoundsRect)


        rect.intersectOrReset(matteBoundsRect)
    }

    private fun applyMasks(canvas: Canvas, matrix: Matrix, frame: Float) {
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
                    applyInvertedAddMask(canvas, matrix, mask, frame)
                } else {
                    applyAddMask(canvas, matrix, mask, frame)
                }

                MaskMode.Subtract -> {
                    if (i == 0) {
                        contentPaint.color = Color.Black
                        contentPaint.alpha = 1f
                        canvas.drawRect(rect, contentPaint)
                    }
                    if (mask.isInverted) {
                        applyInvertedSubtractMask(canvas, matrix, mask, frame)
                    } else {
                        applySubtractMask(canvas, matrix, mask, frame)
                    }
                }

                MaskMode.Intersect -> if (mask.isInverted) {
                    applyInvertedIntersectMask(canvas, matrix, mask, frame)
                } else {
                    applyIntersectMask(canvas, matrix, mask, frame)
                }
            }
        }
        canvas.restore()
    }

    private fun applyInvertedAddMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        canvas.saveLayer(rect, contentPaint)
        canvas.drawRect(rect, contentPaint)
        val maskPath = mask.shape?.interpolated(frame) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolated(frame)?.div(100f)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, dstOutPaint)
        canvas.restore()
    }

    private fun applyAddMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        val maskPath = mask.shape?.interpolated(frame) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolated(frame)?.div(100f)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, contentPaint)
    }

    private fun applySubtractMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        val maskPath = mask.shape?.interpolated(frame) ?: return
        path.set(maskPath)
        path.transform(matrix)
        canvas.drawPath(path, dstOutPaint)
    }

    private fun applyInvertedSubtractMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        canvas.saveLayer(rect, dstOutPaint)
        canvas.drawRect(rect, contentPaint)
        dstOutPaint.alpha = mask.opacity?.interpolated(frame)?.div(100f)?.coerceIn(0f, 1f) ?: 1f
        val maskPath = mask.shape?.interpolated(frame) ?: return
        path.set(maskPath)
        path.transform(matrix)
        canvas.drawPath(path, dstOutPaint)
        canvas.restore()
    }

    private fun applyIntersectMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        canvas.saveLayer(rect, dstInPaint)
        val maskPath = mask.shape?.interpolated(frame) ?: return
        path.set(maskPath)
        path.transform(matrix)
        contentPaint.alpha = mask.opacity?.interpolated(frame)?.div(100f)?.coerceIn(0f, 1f) ?: 1f
        canvas.drawPath(path, contentPaint)
        canvas.restore()
    }

    private fun applyInvertedIntersectMask(
        canvas: Canvas,
        matrix: Matrix,
        mask: Mask,
        frame: Float,
    ) {
        canvas.saveLayer(rect, dstInPaint)
        canvas.drawRect(rect, contentPaint)
        dstOutPaint.alpha = mask.opacity?.interpolated(frame)?.div(100f)?.coerceIn(0f, 1f) ?: 1f
        val maskPath = mask.shape?.interpolated(frame) ?: return
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

