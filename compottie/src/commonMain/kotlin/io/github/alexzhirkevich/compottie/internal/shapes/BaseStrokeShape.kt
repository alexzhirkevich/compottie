package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.toRect
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
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicStrokeProvider
import io.github.alexzhirkevich.compottie.dynamic.applyToPaint
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsState
import io.github.alexzhirkevich.compottie.internal.helpers.DashType
import io.github.alexzhirkevich.compottie.internal.helpers.StrokeDash
import io.github.alexzhirkevich.compottie.internal.helpers.applyTrimPath
import io.github.alexzhirkevich.compottie.internal.platform.ExtendedPathMeasure
import io.github.alexzhirkevich.compottie.internal.platform.GradientCache
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.utils.IdentityMatrix
import io.github.alexzhirkevich.compottie.internal.utils.appendPathEffect
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

    fun asStrokeCap(): StrokeCap {
        return when (this) {
            Butt -> StrokeCap.Butt
            Round -> StrokeCap.Round
            Square -> StrokeCap.Square
            else -> error("Unknown line cap: $this")
        }
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

    fun asStrokeJoin() : StrokeJoin {
        return when(this){
            Miter -> StrokeJoin.Miter
            Round -> StrokeJoin.Round
            Bevel -> StrokeJoin.Bevel
            else -> error("Unknown line join: $this")
        }
    }
}


internal abstract class BaseStrokeShape() : Shape, DrawingContent {

    abstract val opacity: AnimatedNumber
    abstract val strokeWidth: AnimatedNumber
    abstract val lineCap: LineCap
    abstract val lineJoin: LineJoin
    abstract val strokeMiter: Float
    abstract val strokeDash: List<StrokeDash>?

    private val pathGroups = mutableListOf<PathGroup>()

    private val trimPathPath = Path()
    private val path = Path()
    private val rect = MutableRect(0f, 0f, 0f, 0f)
    private val rawBoundsRect = MutableRect(0f, 0f, 0f, 0f)

    protected val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            strokeMiterLimit = strokeMiter
            strokeCap = lineCap.asStrokeCap()
            strokeJoin = lineJoin.asStrokeJoin()
        }
    }
    private val pm = ExtendedPathMeasure()

    private val dashPattern by lazy {
        strokeDash?.filter { it.dashType != DashType.Offset }?.map { it.value }?.let {

            // If there is only 1 value then it is assumed to be equal parts on and off.

            if (it.size == 1)
                it + it[0]
            else it
        }
    }

    private val dashOffset by lazy {
        strokeDash?.firstOrNull { it.dashType == DashType.Offset }?.value
    }

    private val dashPatternValues by lazy {
        FloatArray(dashPattern?.size?.coerceAtLeast(2) ?: 0)
    }

    private var roundShape : RoundShape? = null

    private val effectsState by lazy {
        LayerEffectsState()
    }

    protected var dynamicStroke : DynamicStrokeProvider? = null

    private var dynamicShape: DynamicShapeProvider? = null

    protected var gradientCache = GradientCache()

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState,
    ) {
        if (dynamicShape?.hidden.derive(hidden, state)) {
            return
        }

        paint.pathEffect = null
        paint.style = PaintingStyle.Stroke

        getBounds(drawScope, IdentityMatrix, false, state, rawBoundsRect)

        dynamicStroke.applyToPaint(
            paint = paint,
            state = state,
            parentAlpha = parentAlpha,
            parentMatrix = parentMatrix,
            opacity = opacity,
            strokeWidth = strokeWidth,
            size = rawBoundsRect::toRect,
            gradientCache = gradientCache
        )

        if (paint.strokeWidth <= 0) {
            return
        }

        applyDashPatternIfNeeded(parentMatrix, state)

        state.layer.effectsApplier.applyTo(paint, state, effectsState)

        roundShape?.applyTo(paint, state)

        drawScope.drawIntoCanvas { canvas ->

            pathGroups.fastForEach { pathGroup ->

                if (pathGroup.trimPath != null) {
                    applyTrimPath(canvas, state, pathGroup, parentMatrix)
                } else {
                    path.rewind()
                    pathGroup.paths.fastForEachReversed {
                        path.addPath(it.getPath(state), parentMatrix)
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider?) {
        super.setDynamicProperties(basePath, properties)
        name?.let {
            dynamicStroke = properties?.get(layerPath(basePath, it))
            dynamicShape = properties?.get(layerPath(basePath, it))
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
        state: AnimationState,
        outBounds: MutableRect,
    ) {
        path.rewind()
        pathGroups.fastForEach { pathGroup ->
            pathGroup.paths.fastForEach {
                path.addPath(it.getPath(state), parentMatrix)
            }
        }
        rect.set(path.getBounds())

        val width = strokeWidth.interpolated(state)

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
        state: AnimationState,
        pathGroup: PathGroup,
        parentMatrix: Matrix,
    ) {
        if (pathGroup.trimPath == null) {
            return
        }

        path.rewind()

        pathGroup.paths.fastForEachReversed {
            path.addPath(it.getPath(state).apply { transform(parentMatrix) })
        }
        val animStartValue: Float = pathGroup.trimPath.start.interpolatedNorm(state)
        val animEndValue: Float = pathGroup.trimPath.end.interpolatedNorm(state)
        val animOffsetValue: Float = pathGroup.trimPath.offset.interpolated(state) / 360f

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
            trimPathPath.set(it.getPath(state))
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


    private fun applyDashPatternIfNeeded(parentMatrix: Matrix, state: AnimationState) {


        val dp = dashPattern

        if (dp.isNullOrEmpty()) {
            return
        }

        val scale = parentMatrix.scale

        val o = dashOffset?.interpolated(state)?.times(scale) ?: 0f

        dp.fastForEachIndexed { i, strokeDash ->

            dashPatternValues[i] = strokeDash.interpolated(state)

            // If the value of the dash pattern or gap is too small, the number of individual sections
            // approaches infinity as the value approaches 0.
            // To mitigate this, we essentially put a minimum value on the dash pattern size of 1px
            // and a minimum gap size of 0.01.
            when {
                i % 2 == 0 -> dashPatternValues[i] = dashPatternValues[i].coerceAtLeast(1f)
                i % 2 == 1 -> dashPatternValues[i] = dashPatternValues[i].coerceAtLeast(.01f)
            }

            dashPatternValues[i] = dashPatternValues[i] * scale
        }

        paint.appendPathEffect(PathEffect.dashPathEffect(dashPatternValues, o))
    }
}

private class PathGroup(
    val trimPath: TrimPathShape?,
) {

    val paths: MutableList<PathContent> = mutableListOf()
}