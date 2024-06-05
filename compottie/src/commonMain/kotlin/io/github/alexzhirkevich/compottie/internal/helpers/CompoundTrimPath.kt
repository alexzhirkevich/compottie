package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.platform.applyTrimPath
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape

internal class CompoundTrimPath {
    private val contents: MutableList<TrimPathShape> = mutableListOf()

    fun addTrimPath(trimPath: TrimPathShape) {
        contents.add(trimPath)
    }

    fun apply(path: Path, state: AnimationState) {
        contents.fastForEachReversed {
            path.applyTrimPath(it, state)
        }
    }
}
