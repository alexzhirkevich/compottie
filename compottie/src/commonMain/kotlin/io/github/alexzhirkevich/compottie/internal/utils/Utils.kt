package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.trace
import io.github.alexzhirkevich.compottie.internal.platform.ExtendedPathMeasure
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.schema.shapes.TrimPath
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal object Utils {

    private val pathMeasure = ExtendedPathMeasure()
    private val tempPath = Path()
    private val tempPath2 = Path()

    fun saveLayerCompat(
        canvas: Canvas,
        rect: Rect,
        paint: Paint,
    ) {
       canvas.saveLayer(rect, paint)
    }

    fun applyTrimPathIfNeeded(path: Path, trimPath: TrimPath, frame : Int) {
        if (trimPath.hidden) {
            return
        }
        val start: Float = trimPath.start.interpolated(frame)
        val end: Float = trimPath.end.interpolated(frame)
        val offset: Float = trimPath.offset.interpolated(frame)
        applyTrimPathIfNeeded(
            path = path,
            startValue = start / 100f,
            endValue = end / 100f,
            offsetValue = offset / 360f
        )
    }

    fun applyTrimPathIfNeeded(
        path: Path, startValue: Float, endValue: Float, offsetValue: Float,
    ) {

        pathMeasure.setPath(path, false)

        val length: Float = pathMeasure.length
        if (startValue == 1f && endValue == 0f) {
            return
        }
        if (length < 1f || abs((endValue - startValue - 1).toDouble()) < .01) {
            return
        }
        val start = length * startValue
        val end = length * endValue
        var newStart = min(start.toDouble(), end.toDouble()).toFloat()
        var newEnd = max(start.toDouble(), end.toDouble()).toFloat()

        val offset = offsetValue * length
        newStart += offset
        newEnd += offset

        // If the trim path has rotated around the path, we need to shift it back.
        if (newStart >= length && newEnd >= length) {
            newStart = floorMod(newStart, length).toFloat()
            newEnd = floorMod(newEnd, length).toFloat()
        }

        if (newStart < 0) {
            newStart = floorMod(newStart, length).toFloat()
        }
        if (newEnd < 0) {
            newEnd = floorMod(newEnd, length).toFloat()
        }

        // If the start and end are equals, return an empty path.
        if (newStart == newEnd) {
            path.reset()
            return
        }

        if (newStart >= newEnd) {
            newStart -= length
        }

        tempPath.reset()
        pathMeasure.getSegment(
            newStart,
            newEnd,
            tempPath,
            true
        )

        if (newEnd > length) {
            tempPath2.reset()
            pathMeasure.getSegment(
                0f,
                newEnd % length,
                tempPath2,
                true
            )
            tempPath.addPath(tempPath2)
        } else if (newStart < 0) {
            tempPath2.reset()
            pathMeasure.getSegment(
                length + newStart,
                length,
                tempPath2,
                true
            )
            tempPath.addPath(tempPath2)
        }
        path.set(tempPath)
    }
}