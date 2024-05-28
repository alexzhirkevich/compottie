package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath


actual fun ExtendedPathMeasure() : ExtendedPathMeasure = AndroidExtendedPathMeasure(
    android.graphics.PathMeasure()
)

private val matrix = android.graphics.Matrix()

private class AndroidExtendedPathMeasure(
    private val internalPathMeasure: android.graphics.PathMeasure
) : ExtendedPathMeasure {

    override fun nextContour(): Boolean {
        return internalPathMeasure.nextContour()
    }

    override val length: Float
        get() = internalPathMeasure.length

    private var positionArray: FloatArray? = null

    private var tangentArray: FloatArray? = null

    override fun getSegment(
        startDistance: Float,
        stopDistance: Float,
        destination: Path,
        startWithMoveTo: Boolean
    ): Boolean {
        return internalPathMeasure.getSegment(
            startDistance,
            stopDistance,
            destination.asAndroidPath(),
            startWithMoveTo
        )
    }

    override fun setPath(path: Path?, forceClosed: Boolean) {
        internalPathMeasure.setPath(path?.asAndroidPath(), forceClosed)
    }

    override fun getPosition(
        distance: Float
    ): Offset {
        if (positionArray == null) {
            positionArray = FloatArray(2)
        }
        if (tangentArray == null) {
            tangentArray = FloatArray(2)
        }
        val result = internalPathMeasure.getPosTan(distance, positionArray, tangentArray)
        return if (result) {
            Offset(positionArray!![0], positionArray!![1])
        } else {
            Offset.Unspecified
        }
    }

    override fun getTangent(
        distance: Float
    ): Offset {
        if (positionArray == null) {
            positionArray = FloatArray(2)
        }
        if (tangentArray == null) {
            tangentArray = FloatArray(2)
        }
        val result = internalPathMeasure.getPosTan(distance, positionArray, tangentArray)
        return if (result) {
            Offset(tangentArray!![0], tangentArray!![1])
        } else {
            Offset.Unspecified
        }
    }
}
