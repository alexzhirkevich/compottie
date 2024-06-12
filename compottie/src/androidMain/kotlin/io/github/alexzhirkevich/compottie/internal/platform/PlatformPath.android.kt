package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.setFrom


internal actual fun ExtendedPathMeasure() : ExtendedPathMeasure = AndroidExtendedPathMeasure(
    android.graphics.PathMeasure()
)

private val tempAndroidMatrix = android.graphics.Matrix()
internal actual fun Path.addPath(path: Path, matrix: Matrix) : Path {
    return asAndroidPath().apply {
        addPath(path.asAndroidPath(), tempAndroidMatrix.apply {
            reset()
            setFrom(matrix)
        })
    }.asComposePath()
}

//internal actual fun Path.set(other : Path) {
//    asAndroidPath().set(other.asAndroidPath())
//}


private class AndroidExtendedPathMeasure(
    private val internalPathMeasure: android.graphics.PathMeasure
) : ExtendedPathMeasure {

    override fun nextContour(): Boolean {

        return internalPathMeasure.nextContour()
    }

    override val length: Float
        get() = internalPathMeasure.length

    private val positionArray: FloatArray by lazy {
        FloatArray(2)
    }

    private val tangentArray: FloatArray by lazy {
        FloatArray(2)
    }

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
        val result = internalPathMeasure.getPosTan(distance, positionArray, tangentArray)
        return if (result) {
            Offset(positionArray[0], positionArray[1])
        } else {
            Offset.Unspecified
        }
    }

    override fun getTangent(
        distance: Float
    ): Offset {
        val result = internalPathMeasure.getPosTan(distance, positionArray, tangentArray)
        return if (result) {
            Offset(tangentArray[0], tangentArray[1])
        } else {
            Offset.Unspecified
        }
    }
}

