package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.schema.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.utils.Utils
import io.github.alexzhirkevich.compottie.internal.utils.union

internal class ContentGroup(
    override val name: String?,
    val hidden: Boolean,
    val contents: List<Content>,
    val transform: Transform?,
) : DrawingContent, PathContent {

    private val rect = MutableRect(0f,0f,0f,0f)
    private val offscreenRect = MutableRect(0f,0f,0f,0f)
    private val offscreenPaint = Paint()
    private val matrix = Matrix()
    private val path = Path()
    private var pathContents: MutableList<PathContent>? = null


    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Int) {
        if (hidden) {
            return
        }

        var layerAlpha = parentAlpha

        matrix.setFrom(parentMatrix)


        if (transform != null) {
            matrix *= transform.matrix(frame)
            transform.opacity?.interpolated(frame)?.let {
                layerAlpha *= it
            }
        }

        val isRenderingWithOffScreen = hasTwoOrMoreDrawableContent()

        if (isRenderingWithOffScreen) {
            offscreenRect.set(0f, 0f, 0f, 0f)
            getBounds(offscreenRect, matrix, true, frame)
            offscreenPaint.alpha = layerAlpha
            Utils.saveLayerCompat(canvas, offscreenRect.toRect(), offscreenPaint)
        }

        val childAlpha = if (isRenderingWithOffScreen) 1f else layerAlpha

        for (i in contents.indices.reversed()) {
            val content: Any = contents[i]
            if (content is DrawingContent) {
                content.draw(canvas, matrix, childAlpha, frame)
            }
        }

        if (isRenderingWithOffScreen) {
            canvas.restore()
        }
    }

    override fun getPath(time: Int): Path {

        // TODO: cache this somehow.
        matrix.reset()

        if (transform != null) {
            matrix.setFrom(transform.matrix(time))
        }
        path.reset()
        if (hidden) {
            return path
        }
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is PathContent) {
                path.addPath(content.getPath(time), matrix)
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
        frame: Int,
    ) {
        matrix.setFrom(parentMatrix)
        if (transform != null) {
            matrix *= transform.matrix(frame)
        }

        rect.set(0f,0f,0f,0f)
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is DrawingContent) {
                content.getBounds(rect, matrix, applyParents, frame)
                outBounds.union(rect)
            }
        }
    }


    private fun hasTwoOrMoreDrawableContent(): Boolean {
        var drawableContentCount = 0
        for (i in contents.indices) {
            if (contents[i] is DrawingContent) {
                drawableContentCount += 1
                if (drawableContentCount >= 2) {
                    return true
                }
            }
        }
        return false
    }
}