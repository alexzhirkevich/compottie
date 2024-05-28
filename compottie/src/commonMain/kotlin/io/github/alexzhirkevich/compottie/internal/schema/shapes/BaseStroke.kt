package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.platform.ExtendedPathMeasure
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.schema.Content
import io.github.alexzhirkevich.compottie.internal.schema.DrawableContent
import io.github.alexzhirkevich.compottie.internal.schema.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.properties.TrimPathType
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.Utils
import kotlin.math.min

internal abstract class BaseStroke() : DrawableContent {

    private val pathGroups = mutableListOf<PathGroup>()

    private val trimPathPath = Path()
    private val path = Path()
    private val paint = Paint()
    private val pm = ExtendedPathMeasure()

    abstract val opacity: Value
    abstract val strokeWidth: Value

    open fun setupPaint(paint: Paint, time: Int){
        paint.style = PaintingStyle.Stroke
        paint.alpha = opacity.interpolated(time)
        paint.strokeWidth = strokeWidth.interpolated(time)
    }

    final override fun drawIntoCanvas(canvas: Canvas, parentMatrix: Matrix, time: Int) {

        //TODO:
//        if (com.airbnb.lottie.utils.Utils.hasZeroScaleAxis(parentMatrix)) {
//            return
//        }

        setupPaint(paint, time)

        if (paint.strokeWidth <= 0) {
            return
        }
//        applyDashPatternIfNeeded(parentMatrix)

//        if (colorFilterAnimation != null) {
//            paint.setColorFilter(colorFilterAnimation.getValue())
//        }
//
//        if (blurAnimation != null) {
//            val blurRadius: Float = blurAnimation.getValue()
//            if (blurRadius == 0f) {
//                paint.setMaskFilter(null)
//            } else if (blurRadius != blurMaskFilterRadius) {
//                val blur: BlurMaskFilter = layer.getBlurMaskFilter(blurRadius)
//                paint.setMaskFilter(blur)
//            }
//            blurMaskFilterRadius = blurRadius
//        }
//        if (dropShadowAnimation != null) {
//            dropShadowAnimation.applyTo(paint)
//        }

        pathGroups.fastForEach { pathGroup ->

            if (pathGroup.trimPath != null) {
                applyTrimPath(canvas, time, pathGroup, parentMatrix)
            } else {
                path.reset()
                pathGroup.paths.fastForEachReversed {
                    path.addPath(it.getPath(time).apply { transform(parentMatrix) })
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

        val trimPathContentBefore: TrimPath? = contentsBefore.firstOrNull {
            it is TrimPath && it.type == TrimPathType.Companion.Individually
        } as TrimPath?

        var currentPathGroup: PathGroup? = null

        contentsAfter.fastForEachReversed { content ->
            if (content is TrimPath && content.type == TrimPathType.Individually) {
                currentPathGroup?.let(pathGroups::add)

                currentPathGroup = PathGroup(content)
            } else if (content is PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = PathGroup(trimPathContentBefore)
                }
                currentPathGroup?.paths?.add(content)
            }
        }

        contentsAfter.fastForEachReversed { content ->
            if (content is TrimPath && content.type == TrimPathType.Individually) {
                currentPathGroup?.let(pathGroups::add)

                currentPathGroup = PathGroup(content)

            } else if (content is PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = PathGroup(
                        trimPathContentBefore
                    )
                }
                currentPathGroup!!.paths.add(content)
            }
        }

        currentPathGroup?.let(pathGroups::add)
    }

    private fun applyTrimPath(
        canvas: Canvas,
        time: Int,
        pathGroup: PathGroup,
        parentMatrix: Matrix
    ) {
        if (pathGroup.trimPath == null) {
            return
        }
        path.reset()

        pathGroup.paths.fastForEachReversed {
            path.addPath(it.getPath(time).apply { transform(parentMatrix) })
        }
        val animStartValue: Float = pathGroup.trimPath.start.interpolated(time) / 100f
        val animEndValue: Float = pathGroup.trimPath.end.interpolated(time) / 100f
        val animOffsetValue: Float = pathGroup.trimPath.offset.interpolated(time) / 360f

        // If the start-end is ~100, consider it to be the full path.
        if (animStartValue < 0.01f && animEndValue > 0.99f) {
            canvas.drawPath(path, paint)
            return
        }

        pm.setPath(path, false)

        var totalLength: Float = pm.length

        while (pm.nextContour()) {
            totalLength += pm.length
        }
        val offsetLength = totalLength * animOffsetValue
        val startLength = totalLength * animStartValue + offsetLength
        val endLength = min(
            (totalLength * animEndValue + offsetLength).toDouble(),
            (startLength + totalLength - 1f).toDouble()
        ).toFloat()

        var currentLength = 0f
        for (j in pathGroup.paths.indices.reversed()) {
            trimPathPath.set(pathGroup.paths[j].getPath(time))
            trimPathPath.transform(parentMatrix)
            pm.setPath(trimPathPath, false)
            val length: Float = pm.length
            if (endLength > totalLength && endLength - totalLength < currentLength + length && currentLength < endLength - totalLength) {
                // Draw the segment when the end is greater than the length which wraps around to the
                // beginning.
                val startValue = if (startLength > totalLength) {
                    (startLength - totalLength) / length
                } else {
                    0f
                }
                val endValue =
                    min(((endLength - totalLength) / length).toDouble(), 1.0)
                        .toFloat()
                Utils.applyTrimPathIfNeeded(
                    trimPathPath,
                    startValue,
                    endValue,
                    0f
                )
                canvas.drawPath(trimPathPath, paint)
            } else
                if (currentLength + length < startLength || currentLength > endLength) {
                    // Do nothing
                } else if (currentLength + length <= endLength && startLength < currentLength) {
                    canvas.drawPath(trimPathPath, paint)
                } else {
                    val startValue = if (startLength < currentLength) {
                        0f
                    } else {
                        (startLength - currentLength) / length
                    }
                    val endValue = if (endLength > currentLength + length) {
                        1f
                    } else {
                        (endLength - currentLength) / length
                    }
                    Utils.applyTrimPathIfNeeded(
                        trimPathPath,
                        startValue,
                        endValue,
                        0f
                    )
                    canvas.drawPath(trimPathPath, paint)
                }
            currentLength += length
        }
    }
}

private class PathGroup(
    val trimPath: TrimPath?
) {

    val paths: MutableList<PathContent> = mutableListOf()
}