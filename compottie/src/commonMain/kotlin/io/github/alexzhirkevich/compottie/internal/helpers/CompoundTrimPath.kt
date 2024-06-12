package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.platform.ExtendedPathMeasure
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape
import io.github.alexzhirkevich.compottie.internal.shapes.isSimultaneousTrimPath
import io.github.alexzhirkevich.compottie.internal.utils.floorMod
import kotlin.math.abs

internal class CompoundTrimPath(
    private val contents : List<TrimPathShape>
) {

    fun apply(path: Path, state: AnimationState) {
        contents.fastForEachReversed {
            path.applyTrimPath(it, state)
        }
    }
}

internal fun CompoundSimultaneousTrimPath(contents: List<Content>) : CompoundTrimPath? {
    return contents
        .filterIsInstance<TrimPathShape>()
        .filter(Content::isSimultaneousTrimPath)
        .takeIf(List<*>::isNotEmpty)
        ?.let { CompoundTrimPath(it) }
}

internal fun Path.applyTrimPath(trimPath: TrimPathShape, state: AnimationState) {
    if (trimPath.hidden) {
        return
    }
    val start: Float = trimPath.start.interpolatedNorm(state)
    val end: Float = trimPath.end.interpolatedNorm(state)
    val offset: Float = trimPath.offset.interpolated(state)

    applyTrimPath(
        startValue = start,
        endValue = end,
        offsetValue = offset / 360f
    )
}


private val pathMeasure by lazy {
    ExtendedPathMeasure()
}

private val tempPath = Path()

internal fun Path.applyTrimPath(
    startValue: Float,
    endValue: Float,
    offsetValue: Float,
) {
    pathMeasure.setPath(this, false)

    val length = pathMeasure.length
    if (startValue == 1f && endValue == 0f) {
        return
    }
    if (length < 1f || abs((endValue - startValue - 1)) < .01f) {
        return
    }

    var start = (length * ((startValue + offsetValue) % 1f))
    var end = (length * ((endValue + offsetValue) % 1f))

    if (start >= length && end >= length) {
        start = floorMod(start, length).toFloat()
        end = floorMod(end, length).toFloat()
    }

    if (start < 0) {
        start = floorMod(start, length).toFloat()
    }
    if (end < 0) {
        end = floorMod(end, length).toFloat()
    }
    tempPath.reset()

    if (start > end) {
        pathMeasure.getSegment(start, length, tempPath, true)
        pathMeasure.getSegment(0f, end, tempPath, true)
    } else {
        pathMeasure.getSegment(start, end, tempPath, true)
    }
    set(tempPath)
}

