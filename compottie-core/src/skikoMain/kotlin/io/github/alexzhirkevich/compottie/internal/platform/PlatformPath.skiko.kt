package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.asSkiaPathMeasure
import org.jetbrains.skia.PathBuilder

internal actual fun ExtendedPathMeasure() : ExtendedPathMeasure = SkikoExtendedPathMeasure()

private class SkikoExtendedPathMeasure(
    private val delegate: PathMeasure = PathMeasure()
) : ExtendedPathMeasure, PathMeasure by delegate {

    override fun nextContour(): Boolean {
        return delegate.asSkiaPathMeasure().nextContour()
    }
}

internal actual class PathBuilder actual constructor() : AutoCloseable {
    private val skikoPathBuilder = PathBuilder()

    actual fun addPath(
        path: Path,
        matrix: Matrix
    ) {
        skikoPathBuilder.addPath(path.asSkiaPath(), matrix = matrix.asSkia33())
    }

    actual fun setTo(path: Path) {
        val snapshot = skikoPathBuilder.snapshot()
        path.asSkiaPath().swap(snapshot)
        snapshot.close()
        skikoPathBuilder.reset()
    }

    actual override fun close() {
        skikoPathBuilder.close()
    }
}
