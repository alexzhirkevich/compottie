package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.utils.Utils
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.union

internal class ContentGroup(
    override val name: String?,
    val hidden: Boolean,
    contents: List<Content>,
    val transform: AnimatedTransform?,
) : PathAndDrawingContext {

    private val rect = MutableRect(0f,0f,0f,0f)
    private val offscreenRect = MutableRect(0f,0f,0f,0f)
    private val offscreenPaint = Paint()
    private val matrix = Matrix()
    private val path = Path()
    private var pathContents: MutableList<PathContent>? = null

    private val boundsRect = MutableRect(0f,0f,0f,0f)

    private val contents = contents.toMutableList()

    init {
        val greedyContents = contents.filterIsInstance<GreedyContent>().reversed()

        greedyContents.fastForEachReversed {
            it.absorbContent(this.contents.listIterator(this.contents.size))
        }
    }

    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {

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

        val isRenderingWithOffScreen = hasTwoOrMoreDrawableContent() && layerAlpha < 1f

        drawScope.drawIntoCanvas { canvas ->
            if (isRenderingWithOffScreen) {
                offscreenRect.set(0f, 0f, 0f, 0f)
                getBounds(offscreenRect, matrix, true, frame)
                offscreenPaint.alpha = layerAlpha
                Utils.saveLayerCompat(canvas, offscreenRect.toRect(), offscreenPaint)
            }

            val childAlpha = if (isRenderingWithOffScreen) 1f else layerAlpha

            contents.fastForEachReversed { content ->
                if (content is DrawingContent) {
                    content.draw(drawScope, matrix, childAlpha, frame)
                }
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
        contents.fastForEachReversed {
            if (it is PathContent) {
                path.addPath(it.getPath(frame), matrix)
            }
        }

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        // Do nothing with contents after.
        val myContentsBefore: MutableList<Content> = ArrayList(contentsBefore.size + contents.size)
        myContentsBefore.addAll(contentsBefore)

        for (i in contents.indices.reversed()) {
            val content = contents[i]
            content.setContents(myContentsBefore, contents.subList(0, i))
            myContentsBefore.add(content)
        }
    }

    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
    ) {
        matrix.setFrom(parentMatrix)
        if (transform != null) {
            matrix.preConcat(transform.matrix(frame))
        }
        rect.set(0f, 0f, 0f, 0f)

        contents.fastForEachReversed {
            if (it is DrawingContent) {
                it.getBounds(rect, matrix, applyParents, frame)
                outBounds.union(rect)
            }
        }
    }


    private fun hasTwoOrMoreDrawableContent(): Boolean {
        var drawableContentCount = 0
        contents.fastForEach {
            if (it is DrawingContent) {
                drawableContentCount += 1
                if (drawableContentCount >= 2) {
                    return true
                }
            }
        }
        return false
    }
}