package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.union

internal class ContentGroup(
    override val name: String?,
    val hidden: Boolean,
    contents: List<Content>,
    override val transform: AnimatedTransform?,
) : ContentGroupBase {

    private val rect = MutableRect(0f, 0f, 0f, 0f)
    private val offscreenRect = MutableRect(0f, 0f, 0f, 0f)
    private val offscreenPaint = Paint().apply {
        isAntiAlias = true
    }
    private val matrix = Matrix()
    private val path = Path()
    private val boundsRect = MutableRect(0f, 0f, 0f, 0f)

    private val mContents by lazy {
        contents.filter {
            !(it is ContentGroupBase && it.pathContents.isEmpty())
        }.toMutableList()
    }

    override val pathContents: List<PathContent> by lazy {
        this.mContents.filterIsInstance<PathContent>()
    }

    private val drawingContents: List<DrawingContent> by lazy {
        this.mContents.filterIsInstance<DrawingContent>()
    }

    init {
        val greedyContents = contents.filterIsInstance<GreedyContent>().reversed()

        greedyContents.fastForEachReversed {
            it.absorbContent(this.mContents.listIterator(this.mContents.size))
        }
    }

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float
    ) {
        if (hidden) {
            return
        }

        var layerAlpha = parentAlpha

        matrix.setFrom(parentMatrix)

        if (transform != null) {
            matrix.preConcat(transform.matrix(frame))
            transform.opacity?.interpolated(frame)?.let {

                layerAlpha = (layerAlpha * it / 100f).coerceIn(0f, 1f)
            }
        }

        val isRenderingWithOffScreen = drawingContents.size > 2 && layerAlpha < 1f

        drawScope.drawIntoCanvas { canvas ->
            if (isRenderingWithOffScreen) {
                offscreenRect.set(0f, 0f, 0f, 0f)
                getBounds(drawScope, matrix, true, frame, offscreenRect)
                offscreenPaint.alpha = layerAlpha
                canvas.saveLayer(offscreenRect.toRect(), offscreenPaint)
            }

            val childAlpha = if (isRenderingWithOffScreen) 1f else layerAlpha

            drawingContents.fastForEachReversed { content ->
                content.draw(drawScope, matrix, childAlpha, frame)
            }

            if (isRenderingWithOffScreen) {
                canvas.restore()
            }
        }
    }

    override fun getPath(frame: Float): Path {

        path.reset()
        if (hidden) {
            return path
        }
        matrix.reset()

        if (transform != null) {
            matrix.setFrom(transform.matrix(frame))
        }
        pathContents.fastForEachReversed {
            path.addPath(it.getPath(frame), matrix)
        }

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        // Do nothing with contents after.
        val myContentsBefore: MutableList<Content> = ArrayList(contentsBefore.size + mContents.size)
        myContentsBefore.addAll(contentsBefore)

        for (i in mContents.indices.reversed()) {
            val content = mContents[i]
            content.setContents(myContentsBefore, mContents.subList(0, i))
            myContentsBefore.add(content)
        }
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
        outBounds: MutableRect,
    ) {
        matrix.setFrom(parentMatrix)
        if (transform != null) {
            matrix.preConcat(transform.matrix(frame))
        }
        rect.set(0f, 0f, 0f, 0f)

        drawingContents.fastForEachReversed {
            it.getBounds(drawScope, matrix, applyParents, frame, rect)
            outBounds.union(rect)
        }
    }
}