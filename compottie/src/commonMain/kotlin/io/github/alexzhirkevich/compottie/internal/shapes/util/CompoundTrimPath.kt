package io.github.alexzhirkevich.compottie.internal.shapes.util

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape
import io.github.alexzhirkevich.compottie.internal.utils.Utils

internal class CompoundTrimPath {
    private val contents: MutableList<TrimPathShape> = mutableListOf()

    fun addTrimPath(trimPath: TrimPathShape) {
        contents.add(trimPath)
    }

    fun apply(path: Path, frame: Float) {
        contents.fastForEachReversed {
            Utils.applyTrimPathIfNeeded(path, it, frame)
        }
    }
}
