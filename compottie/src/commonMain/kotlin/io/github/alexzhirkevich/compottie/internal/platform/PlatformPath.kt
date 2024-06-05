package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape
import io.github.alexzhirkevich.compottie.internal.utils.floorMod
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal expect fun ExtendedPathMeasure() : ExtendedPathMeasure

internal interface ExtendedPathMeasure : PathMeasure {
    fun nextContour() : Boolean
}

internal fun Path.set(other : Path){
    reset()
    addPath(other)
}

//internal expect fun Path.set(other : Path)

internal expect fun Path.addPath(path: Path, matrix: Matrix) : Path

internal fun Path.applyTrimPath(trimPath: TrimPathShape, state: AnimationState) {
    if (trimPath.hidden) {
        return
    }
    val start: Float = trimPath.start.interpolated(state)
    val end: Float = trimPath.end.interpolated(state)
    val offset: Float = trimPath.offset.interpolated(state)

    applyTrimPath(
        startValue = start / 100f,
        endValue = end / 100f,
        offsetValue = offset / 360f
    )
}


private val pathMeasure = ExtendedPathMeasure()
private val tempPath = Path()
private val tempPath2 = Path()

fun Path.applyTrimPath(
    startValue: Float,
    endValue: Float,
    offsetValue: Float,
) {
    pathMeasure.setPath(this, false)

    val length: Float = pathMeasure.length
    if (startValue == 1f && endValue == 0f) {
        return
    }
    if (length < 1f || abs((endValue - startValue - 1)) < .01f) {
        return
    }
    val start = length * startValue
    val end = length * endValue
    var newStart = min(start, end)
    var newEnd = max(start, end)

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
        reset()
        return
    }

    if (newStart >= newEnd) {
        newStart -= length
    }


    tempPath.reset()
    pathMeasure.getSegment(
        startDistance = newStart,
        stopDistance = newEnd,
        destination = tempPath,
        startWithMoveTo = true
    )

    if (newEnd > length) {
        tempPath2.reset()
        pathMeasure.getSegment(
            startDistance = 0f,
            stopDistance = newEnd % length,
            destination = tempPath2,
            startWithMoveTo = true
        )
        tempPath.addPath(tempPath2)
    } else if (newStart < 0) {
        tempPath2.reset()
        pathMeasure.getSegment(
            startDistance = length + newStart,
            stopDistance = length,
            destination = tempPath2,
            startWithMoveTo = true
        )
        tempPath.addPath(tempPath2)
    }

    set(tempPath)
}