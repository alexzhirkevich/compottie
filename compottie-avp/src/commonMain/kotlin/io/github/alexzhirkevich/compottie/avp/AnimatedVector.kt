package io.github.alexzhirkevich.compottie.avp

import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.DefaultPivotX
import androidx.compose.ui.graphics.vector.DefaultPivotY
import androidx.compose.ui.graphics.vector.DefaultRotation
import androidx.compose.ui.graphics.vector.DefaultScaleX
import androidx.compose.ui.graphics.vector.DefaultScaleY
import androidx.compose.ui.graphics.vector.DefaultStrokeLineMiter
import androidx.compose.ui.graphics.vector.DefaultStrokeLineWidth
import androidx.compose.ui.graphics.vector.DefaultTranslationX
import androidx.compose.ui.graphics.vector.DefaultTranslationY
import androidx.compose.ui.graphics.vector.DefaultTrimPathEnd
import androidx.compose.ui.graphics.vector.DefaultTrimPathOffset
import androidx.compose.ui.graphics.vector.DefaultTrimPathStart
import androidx.compose.ui.graphics.vector.EmptyPath
import androidx.compose.ui.graphics.vector.VNode
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.avp.animator.StaticFloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.StaticPathAnimator

internal val DefaultRotationAnimator = StaticFloatAnimator(DefaultRotation)
internal val DefaultPivotXAnimator = StaticFloatAnimator(DefaultPivotX)
internal val DefaultPivotYAnimator = StaticFloatAnimator(DefaultPivotY)
internal val DefaultScaleXAnimator = StaticFloatAnimator(DefaultScaleX)
internal val DefaultScaleYAnimator = StaticFloatAnimator(DefaultScaleY)
internal val DefaultTranslationXAnimator = StaticFloatAnimator(DefaultTranslationX)
internal val DefaultTranslationYAnimator = StaticFloatAnimator(DefaultTranslationY)
internal val EmptyPathAnimator = StaticPathAnimator(EmptyPath)

internal val DefaultTrimPathStartAnimator = StaticFloatAnimator(DefaultTrimPathStart)
internal val DefaultTrimPathEndAnimator = StaticFloatAnimator(DefaultTrimPathEnd)
internal val DefaultTrimPathOffsetAnimator = StaticFloatAnimator(DefaultTrimPathOffset)
internal val DefaultStrokeLineMiterAnimator = StaticFloatAnimator(DefaultStrokeLineMiter)
internal val DefaultStrokeLineWidthAnimator = StaticFloatAnimator(DefaultStrokeLineWidth)
internal val DefaultAlphaAnimator = StaticFloatAnimator(DefaultAlpha)

internal sealed class AnimatedVNode {

    abstract fun DrawScope.draw(time : Float)
}

internal class AnimatedPathComponent(
    private val vector : AnimatedVectorPath
) : AnimatedVNode() {

    private var renderPath  = Path()

    private val strokePaint = Paint().apply {
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
    }

    private val pathMeasure: PathMeasure by lazy(LazyThreadSafetyMode.NONE) { PathMeasure() }

    private fun updateRenderPath(time : Float, path: Path)  {
        val trimPathStart = vector.trimPathStart.animate(time)
        val trimPathEnd = vector.trimPathEnd.animate(time)

        if (trimPathStart == DefaultTrimPathStart && trimPathEnd == DefaultTrimPathEnd) {
            renderPath = path
        } else {
            if (renderPath == path) {
                renderPath = Path()
            } else {
                // Rewind unsets the fill type so reset it here
                val fillType = renderPath.fillType
                renderPath.rewind()
                renderPath.fillType = fillType
            }

            pathMeasure.setPath(path, false)
            val trimPathOffset = vector.trimPathOffset.animate(time)
            val length = pathMeasure.length
            val start = ((trimPathStart + trimPathOffset) % 1f) * length
            val end = ((trimPathEnd + trimPathOffset) % 1f) * length
            if (start > end) {
                pathMeasure.getSegment(start, length, renderPath, true)
                pathMeasure.getSegment(0f, end, renderPath, true)
            } else {
                pathMeasure.getSegment(start, end, renderPath, true)
            }
        }
    }

    private fun updateFill(time : Float){
        vector.fill?.let {
            val data = it.animate(time)
            fillPaint.color = data.color
            fillPaint.shader = data.shader
            fillPaint.alpha = vector.fillAlpha.animate(time)
        }
    }

    private fun updateStroke(time : Float){
        vector.stroke?.let {
            val data = it.animate(time)
            strokePaint.color = data.color
            strokePaint.shader = data.shader
            strokePaint.alpha = vector.strokeAlpha.animate(time)
            strokePaint.strokeWidth = vector.strokeLineWidth.animate(time)
            strokePaint.strokeMiterLimit = vector.strokeLineMiter.animate(time)
            strokePaint.strokeCap = vector.strokeLineCap
            strokePaint.strokeJoin = vector.strokeLineJoin
        }
    }

    override fun DrawScope.draw(time: Float) {
        updateFill(time)
        updateStroke(time)
        updateRenderPath(time,vector.pathData.animate(time))

        if (vector.fill != null){
            drawContext.canvas.drawPath(renderPath, fillPaint)
        }
        if (vector.stroke != null){
            drawContext.canvas.drawPath(renderPath, strokePaint)
        }
    }
}

internal class AnimatedGroupComponent(
    private val group: AnimatedVectorGroup
) : AnimatedVNode() {
    private var groupMatrix: Matrix? = null

    private val children = mutableListOf<VNode>()

    private val willClipPath: Boolean
        get() = group.clipPathData !is StaticPathAnimator || group.clipPathData.value.isNotEmpty()

    private fun updateMatrix(time: Float) {
        val matrix: Matrix
        val target = groupMatrix
        if (target == null) {
            matrix = Matrix()
            groupMatrix = matrix
        } else {
            matrix = target
            matrix.reset()
        }
        // M = T(translationX + pivotX, translationY + pivotY) *
        //     R(rotation) * S(scaleX, scaleY) *
        //     T(-pivotX, -pivotY)
        val pivotX = group.pivotX.animate(time)
        val pivotY = group.pivotY.animate(time)
        matrix.translate(group.translationX.animate(time) + pivotX, group.translationY.animate(time) + pivotY)
        matrix.rotateZ(degrees = group.rotation.animate(time))
        matrix.scale(group.scaleX.animate(time), group.scaleY.animate(time), 1f)
        matrix.translate(-pivotX, -pivotY)
    }


    override fun DrawScope.draw(time: Float) {
        updateMatrix(time)

        val clipPath = group.clipPathData.animate(time = time)

        withTransform({
            groupMatrix?.let { transform(it) }
            if (willClipPath) {
                clipPath(clipPath)
            }
        }) {
            children.fastForEach { node ->
                with(node) {
                    this@draw.draw()
                }
            }
        }
    }
}
