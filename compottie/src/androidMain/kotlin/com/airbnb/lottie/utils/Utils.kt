package com.airbnb.lottie.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import com.airbnb.lottie.L
import com.airbnb.lottie.animation.content.TrimPathContent
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object Utils {
    const val SECOND_IN_NANOS: Int = 1000000000

    private val INV_SQRT_2 = (sqrt(2.0) / 2.0).toFloat()

//    fun createPath(startPoint: Offset, endPoint: Offset, cp1: Offset?, cp2: Offset?): Path {
//        val path = Path()
//        path.moveTo(startPoint.x, startPoint.y)
//
//        if (cp1 != null && cp2 != null && (cp1.length() != 0f || cp2.length() != 0f)) {
//            path.cubicTo(
//                startPoint.x + cp1.x, startPoint.y + cp1.y,
//                endPoint.x + cp2.x, endPoint.y + cp2.y,
//                endPoint.x, endPoint.y
//            )
//        } else {
//            path.lineTo(endPoint.x, endPoint.y)
//        }
//        return path
//    }


    fun getScale(matrix: Matrix): Float {

        val rect = Rect(0f,0f,INV_SQRT_2,INV_SQRT_2)
        val points = FloatArray(4)

        points[0] = 0f
        points[1] = 0f
        // Use 1/sqrt(2) so that the hypotenuse is of length 1.
        points[2] = INV_SQRT_2
        points[3] = INV_SQRT_2

        matrix.mapPoints(points)
        val dx = points[2] - points[0]
        val dy = points[3] - points[1]

        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    fun hasZeroScaleAxis(matrix: Matrix): Boolean {
        val points = FloatArray(4)

        points[0] = 0f
        points[1] = 0f
        // Random numbers. The only way these should map to the same thing as 0,0 is if the scale is 0.
        points[2] = 37394.729378f
        points[3] = 39575.2343807f
        matrix.mapPoints(points)
        return points[0] == points[2] || points[1] == points[3]
    }

    fun applyTrimPathIfNeeded(path: Path, trimPath: TrimPathContent?) {
        if (trimPath == null || trimPath.isHidden) {
            return
        }
        val start = (trimPath.start as FloatKeyframeAnimation).floatValue
        val end = (trimPath.end as FloatKeyframeAnimation).floatValue
        val offset = (trimPath.offset as FloatKeyframeAnimation).floatValue
        applyTrimPathIfNeeded(path, start / 100f, end / 100f, offset / 360f)
    }

    fun applyTrimPathIfNeeded(
        path: Path, startValue: Float, endValue: Float, offsetValue: Float
    ) {
        if (L.isTraceEnabled()) {
            L.beginSection("applyTrimPathIfNeeded")
        }
        val pathMeasure = PathMeasure()
        val tempPath = Path()

        pathMeasure.setPath(path, false)

        val length = pathMeasure.length
        if (startValue == 1f && endValue == 0f) {
            if (L.isTraceEnabled()) {
                L.endSection("applyTrimPathIfNeeded")
            }
            return
        }
        if (length < 1f || abs((endValue - startValue - 1).toDouble()) < .01) {
            if (L.isTraceEnabled()) {
                L.endSection("applyTrimPathIfNeeded")
            }
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
            newStart = MiscUtils.floorMod(newStart, length).toFloat()
            newEnd = MiscUtils.floorMod(newEnd, length).toFloat()
        }

        if (newStart < 0) {
            newStart = MiscUtils.floorMod(newStart, length).toFloat()
        }
        if (newEnd < 0) {
            newEnd = MiscUtils.floorMod(newEnd, length).toFloat()
        }

        // If the start and end are equals, return an empty path.
        if (newStart == newEnd) {
            path.reset()
            if (L.isTraceEnabled()) {
                L.endSection("applyTrimPathIfNeeded")
            }
            return
        }

        if (newStart >= newEnd) {
            newStart -= length
        }

        path.reset()
        pathMeasure.getSegment(
            newStart,
            newEnd,
            path,
            true
        )

        if (newEnd > length) {
            tempPath.reset()
            pathMeasure.getSegment(
                0f,
                newEnd % length,
                tempPath,
                true
            )
            path.addPath(tempPath)
        } else if (newStart < 0) {
            tempPath.reset()
            pathMeasure.getSegment(
                length + newStart,
                length,
                tempPath,
                true
            )
            path.addPath(tempPath)
        }
        if (L.isTraceEnabled()) {
            L.endSection("applyTrimPathIfNeeded")
        }
    }

    fun isAtLeastVersion(
        major: Int,
        minor: Int,
        patch: Int,
        minMajor: Int,
        minMinor: Int,
        minPatch: Int
    ): Boolean {
        if (major < minMajor) {
            return false
        } else if (major > minMajor) {
            return true
        }

        if (minor < minMinor) {
            return false
        } else if (minor > minMinor) {
            return true
        }

        return patch >= minPatch
    }

    fun hashFor(a: Float, b: Float, c: Float, d: Float): Int {
        var result = 17
        if (a != 0f) {
            result = (31 * result * a).toInt()
        }
        if (b != 0f) {
            result = (31 * result * b).toInt()
        }
        if (c != 0f) {
            result = (31 * result * c).toInt()
        }
        if (d != 0f) {
            result = (31 * result * d).toInt()
        }
        return result
    }

    fun dpScale(): Float {
        return Resources.getSystem().displayMetrics.density
    }

    fun getAnimationScale(context: Context): Float {
        return 1f
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            Settings.Global.getFloat(
//                context.contentResolver,
//                Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f
//            )
//        } else {
//            Settings.System.getFloat(
//                context.contentResolver,
//                Settings.System.ANIMATOR_DURATION_SCALE, 1.0f
//            )
//        }
    }

    /**
     * Resize the bitmap to exactly the same size as the specified dimension, changing the aspect ratio if needed.
     * Returns the original bitmap if the dimensions already match.
     */
    fun resizeBitmapIfNeeded(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        if (bitmap.width == width && bitmap.height == height) {
            return bitmap
        }
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        bitmap.recycle()
        return resizedBitmap
    }
}

fun Matrix.mapPoints(floatArray: FloatArray) {
    TODO("mapPoints")
}