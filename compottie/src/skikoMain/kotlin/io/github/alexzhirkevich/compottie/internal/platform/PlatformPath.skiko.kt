package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.asSkiaPathMeasure


internal actual fun ExtendedPathMeasure() : ExtendedPathMeasure = SkikoExtendedPathMeasure()

private class SkikoExtendedPathMeasure(
    private val delegate: PathMeasure = PathMeasure()
) : ExtendedPathMeasure, PathMeasure by delegate {

    override fun nextContour(): Boolean {
        return delegate.asSkiaPathMeasure().nextContour()
    }
}


internal actual fun Path.addPath(path: Path, matrix: Matrix) =
    asSkiaPath().addPath(path.asSkiaPath(), matrix = matrix.asSkia33()).asComposePath()