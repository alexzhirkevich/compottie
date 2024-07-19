package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import io.github.alexzhirkevich.compottie.internal.utils.fastSetFrom
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.union

internal class ContentGroupImpl(
    contents: List<Content>,
    override val name: String?,
    private val hidden : ((AnimationState) -> Boolean)?,
    override val transform: AnimatedTransform,
) : ContentGroup {

    private val rect = MutableRect(0f, 0f, 0f, 0f)
    private val offscreenRect = MutableRect(0f, 0f, 0f, 0f)
    private val offscreenPaint = Paint().apply {
    }
    private val matrix = Matrix()
    private val path = Path()

    override fun hidden(state: AnimationState): Boolean {
        return hidden?.invoke(state) == true
    }

    private val mContents = contents
        .filterNot { it is ContentGroup && it.isEmpty }
        .toMutableList()

    override val isEmpty: Boolean
        get() = pathContents.isEmpty() && drawingContents.isEmpty()

    private val pathContents: List<PathContent> = mContents
        .filterIsInstance<PathContent>()

    private val drawingContents: List<DrawingContent> =
        mContents.filterIsInstance<DrawingContent>()

    init {
        val greedyContents = mContents.filterIsInstance<GreedyContent>().reversed()

        greedyContents.fastForEachReversed {
            it.absorbContent(mContents)
        }
    }

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {

        if (hidden(state)) {
            return
        }

        var layerAlpha = parentAlpha

        matrix.fastSetFrom(parentMatrix)

        matrix.preConcat(transform.matrix(state))
        transform.opacity.interpolatedNorm(state).let {

            layerAlpha = (layerAlpha * it).coerceIn(0f, 1f)
        }

        val isRenderingWithOffScreen = state.applyOpacityToLayers &&
                drawingContents.size > 1 && layerAlpha < .99f

        val canvas = drawScope.drawContext.canvas
        if (isRenderingWithOffScreen) {
            offscreenRect.set(0f,0f,0f,0f)
            getBounds(drawScope, matrix, true, state, offscreenRect)
            offscreenPaint.alpha = layerAlpha
            canvas.saveLayer(offscreenRect.toRect(), offscreenPaint)
        }

        val childAlpha = if (isRenderingWithOffScreen) 1f else layerAlpha

        drawingContents.fastForEachReversed { content ->
            content.draw(drawScope, matrix, childAlpha, state)
        }

        if (isRenderingWithOffScreen) {
            canvas.restore()
        }
    }

    override fun getPath(state: AnimationState): Path {

        path.rewind()
        if (hidden(state)) {
            return path
        }
        matrix.fastReset()

        matrix.fastSetFrom(transform.matrix(state))
        pathContents.fastForEachReversed {
            path.addPath(it.getPath(state), matrix)
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
        state: AnimationState,
        outBounds: MutableRect,
    ) {
        matrix.fastSetFrom(parentMatrix)
        matrix.preConcat(transform.matrix(state))
        rect.set(0f, 0f, 0f, 0f)

        drawingContents.fastForEachReversed {
            it.getBounds(drawScope, matrix, applyParents, state, rect)
            outBounds.union(rect)
        }
    }
}