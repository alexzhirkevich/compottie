//package io.github.alexzhirkevich.compottie.internal.shapes.util
//
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.PathMeasure
//import androidx.compose.ui.graphics.PathOperation
//import io.github.alexzhirkevich.compottie.internal.platform.set
//import io.github.alexzhirkevich.compottie.internal.shapes.TrimPath
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
//internal object Utils {
//
//    fun applyTrimPathIfNeeded(
//        path: Path,
//        trimPath: TrimPath,
//        time : Int
//    ) {
//        if (trimPath.hidden) {
//            return
//        }
//        val start: Float = trimPath.start.interpolated(time)
//        val end: Float = trimPath.end.interpolated(time)
//        val offset: Float = trimPath.offset.interpolated(time)
//        applyTrimPathIfNeeded(
//            path,
//            start / 100f,
//            end / 100f,
//            offset / 360f
//        )
//    }
//
//    private val pathMeasure = PathMeasure()
//    private val tempPath = Path()
//    private val tempPath2 = Path()
//
//    fun applyTrimPathIfNeeded(
//        path: Path, startValue: Float, endValue: Float, offsetValue: Float
//    ) {
//
//        pathMeasure.setPath(path, false)
//
//        val length: Float = pathMeasure.length
//
//        if (startValue == 1f && endValue == 0f) {
//            return
//        }
//        if (length < 1f || abs((endValue - startValue - 1).toDouble()) < .01) {
//            return
//        }
//
//        val start = length * startValue
//        val end = length * endValue
//        var newStart = min(start.toDouble(), end.toDouble()).toFloat()
//        var newEnd = max(start.toDouble(), end.toDouble()).toFloat()
//
//        val offset = offsetValue * length
//        newStart += offset
//        newEnd += offset
//
//        // If the trim path has rotated around the path, we need to shift it back.
//        if (newStart >= length && newEnd >= length) {
//            newStart = floorMod(newStart, length).toFloat()
//            newEnd = floorMod(newEnd, length).toFloat()
//        }
//
//        if (newStart < 0) {
//            newStart = floorMod(newStart, length).toFloat()
//        }
//        if (newEnd < 0) {
//            newEnd = floorMod(newEnd, length).toFloat()
//        }
//
//        // If the start and end are equals, return an empty path.
//        if (newStart == newEnd) {
//            path.reset()
//            return
//        }
//
//        if (newStart >= newEnd) {
//            newStart -= length
//        }
//
//        tempPath.reset()
//        pathMeasure.getSegment(
//            newStart,
//            newEnd,
//            tempPath,
//            true
//        )
//
//        if (newEnd > length) {
//            tempPath2.reset()
//            pathMeasure.getSegment(
//                0f,
//                newEnd % length,
//                tempPath2,
//                true
//            )
//            tempPath.addPath(tempPath2)
//        } else if (newStart < 0) {
//            tempPath2.reset()
//            pathMeasure.getSegment(
//                length + newStart,
//                length,
//                tempPath2,
//                true
//            )
//            tempPath.addPath(tempPath2)
//        }
//
//        path.reset()
//        path.set(tempPath)
//    }
//}