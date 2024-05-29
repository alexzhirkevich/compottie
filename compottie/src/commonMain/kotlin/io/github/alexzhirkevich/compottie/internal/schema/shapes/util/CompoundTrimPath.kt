package io.github.alexzhirkevich.compottie.internal.schema.shapes.util

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.schema.shapes.TrimPath
import io.github.alexzhirkevich.compottie.internal.utils.Utils

internal class CompoundTrimPath {
    private val contents: MutableList<TrimPath> = mutableListOf()

    fun addTrimPath(trimPath: TrimPath) {
        contents.add(trimPath)
    }

    fun apply(path: Path, frame : Int) {
        contents.fastForEachReversed {
            Utils.applyTrimPathIfNeeded(path, it, frame)
        }
    }
}
