package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asSkiaPathMeasure


actual fun ExtendedPathMeasure() : ExtendedPathMeasure = SkikoExtendedPathMeasure()

private class SkikoExtendedPathMeasure(
    private val delegate: PathMeasure = PathMeasure()
) : ExtendedPathMeasure, PathMeasure by delegate {

    override fun nextContour(): Boolean {
        return delegate.asSkiaPathMeasure().nextContour()
    }
}
