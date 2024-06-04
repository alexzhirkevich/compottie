package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.helpers.DashType
import io.github.alexzhirkevich.compottie.internal.helpers.StrokeDash
import io.github.alexzhirkevich.compottie.internal.platform.ExtendedPathMeasure
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.platform.applyTrimPath
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.utils.scale
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.min


@Serializable
@JvmInline
internal value class LineCap(val type : Byte) {
    companion object {
        val Butt = LineCap(1)
        val Round = LineCap(2)
        val Square = LineCap(3)
    }
}

@Serializable
@JvmInline
internal value class LineJoin(val type : Byte) {
    companion object {
        val Miter = LineJoin(1)
        val Round = LineJoin(2)
        val Bevel = LineJoin(3)
    }
}

internal fun LineJoin.asStrokeJoin() : StrokeJoin {
    return when(this){
        LineJoin.Miter -> StrokeJoin.Miter
        LineJoin.Round -> StrokeJoin.Round
        LineJoin.Bevel -> StrokeJoin.Bevel
        else -> StrokeJoin.Round// error("Unknown line join: $this")
    }
}

internal fun LineCap.asStrokeCap() : StrokeCap {
    return when(this){
        LineCap.Butt -> StrokeCap.Butt
        LineCap.Round -> StrokeCap.Round
        LineCap.Square -> StrokeCap.Square
        else -> StrokeCap.Round //error("Unknown line cap: $this")
    }
}

internal abstract class BaseStrokeShape() : Shape, DrawingContent {

    abstract val opacity: AnimatedValue
    abstract val strokeWidth: AnimatedValue
    abstract val lineCap: LineCap
    abstract val lineJoin: LineJoin
    abstract val strokeMiter: Float
    abstract val strokeDash: List<StrokeDash>?

    private val pathGroups = mutableListOf<PathGroup>()


    private val trimPathPath = Path()
    private val path = Path()
    private val rect = MutableRect(0f, 0f, 0f, 0f)
    protected val paint = Paint().apply {
        isAntiAlias = true
        strokeMiterLimit = strokeMiter
        strokeCap = lineCap.asStrokeCap()
        strokeJoin = lineJoin.asStrokeJoin()
    }
    private val pm = ExtendedPathMeasure()

    private val dashPattern by lazy {
        strokeDash?.filter { it.dashType != DashType.Offset }?.map { it.value }
    }

    private val dashOffset by lazy {
        strokeDash?.first { it.dashType == DashType.Offset }?.value
    }

    private val dashPatternValues by lazy {
        FloatArray(dashPattern?.size ?: 0)
    }

    private var roundShape : RoundShape? = null

    private var lastBlurRadius : Float? = null

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float
    ) {

        if (hidden){
            return
        }

        paint.style = PaintingStyle.Stroke
        paint.alpha = parentAlpha * (opacity.interpolated(frame) / 100f).coerceIn(0f, 1f)
        paint.strokeWidth = strokeWidth.interpolated(frame)

        if (paint.strokeWidth <= 0) {
            return
        }

        applyDashPatternIfNeeded(parentMatrix, frame)

        lastBlurRadius = layer.applyBlurEffectIfNeeded(paint, frame, lastBlurRadius)

        roundShape?.applyTo(paint, frame)

        drawScope.drawIntoCanvas { canvas ->

            pathGroups.fastForEach { pathGroup ->

                if (pathGroup.trimPath != null) {
                    applyTrimPath(canvas, frame, pathGroup, parentMatrix)
                } else {
                    path.reset()
                    pathGroup.paths.fastForEachReversed {
                        path.addPath(it.getPath(frame), parentMatrix)
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

        val trimPathContentBefore: TrimPathShape? = contentsBefore
            .firstOrNull(Content::isIndividualTrimPath) as TrimPathShape?

        var currentPathGroup: PathGroup? = null

        contentsAfter.fastForEachReversed { content ->
            if (content.isIndividualTrimPath()) {

                currentPathGroup?.let(pathGroups::add)

                currentPathGroup = PathGroup(content)

            } else if (content is PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = PathGroup(trimPathContentBefore)
                }
                currentPathGroup!!.paths.add(content)
            } else if (content is RoundShape){
                roundShape = content
            }
        }

        currentPathGroup?.let(pathGroups::add)
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
        outBounds: MutableRect,
    ) {
        path.reset()
        pathGroups.fastForEach { pathGroup ->
            pathGroup.paths.fastForEach {
                path.addPath(it.getPath(frame), parentMatrix)
            }
        }
        rect.set(path.getBounds())

        val width = strokeWidth.interpolated(frame)

        rect.set(
            rect.left - width / 2f,
            rect.top - width / 2f,
            rect.right + width / 2f,
            rect.bottom + width / 2f
        )
        outBounds.set(rect)

        // Add padding to account for rounding errors.
        outBounds.set(
            outBounds.left - 1,
            outBounds.top - 1,
            outBounds.right + 1,
            outBounds.bottom + 1
        )
    }

    private fun applyTrimPath(
        canvas: Canvas,
        time: Float,
        pathGroup: PathGroup,
        parentMatrix: Matrix,
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

        pathGroup.paths.fastForEachReversed {
            trimPathPath.set(it.getPath(time))
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
                val endValue = min(((endLength - totalLength) / length), 1f)
                trimPathPath.applyTrimPath(startValue, endValue, 0f)
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
                    trimPathPath.applyTrimPath(startValue, endValue, 0f)
                    canvas.drawPath(trimPathPath, paint)
                }
            currentLength += length
        }
    }


    private fun applyDashPatternIfNeeded(parentMatrix: Matrix, frame: Float) {

        val dp = dashPattern
        val o = dashOffset?.interpolated(frame) ?: 0f
        if (dp.isNullOrEmpty()) {
            return
        }

        val scale = parentMatrix.scale

        dp.fastForEachIndexed { i, strokeDash ->
            dashPatternValues[i] = strokeDash.interpolated(frame)
            // If the value of the dash pattern or gap is too small, the number of individual sections
            // approaches infinity as the value approaches 0.
            // To mitigate this, we essentially put a minimum value on the dash pattern size of 1px
            // and a minimum gap size of 0.01.
            if (i % 2 == 0) {
                if (dashPatternValues[i] < 1f) {
                    dashPatternValues[i] = 1f
                }
            } else {
                if (dashPatternValues[i] < 0.1f) {
                    dashPatternValues[i] = 0.1f
                }
            }
            dashPatternValues[i] = dashPatternValues[i] * scale
        }

        paint.pathEffect = PathEffect.dashPathEffect(dashPatternValues, o)
    }
}

private class PathGroup(
    val trimPath: TrimPathShape?,
) {

    val paths: MutableList<PathContent> = mutableListOf()
}