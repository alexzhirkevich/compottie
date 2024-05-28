package com.airbnb.lottie.animation.content

import androidx.compose.ui.graphics.Path
import com.airbnb.lottie.utils.Utils.applyTrimPathIfNeeded

class CompoundTrimPathContent {
    private val contents: MutableList<TrimPathContent> = ArrayList()

    fun addTrimPath(trimPath: TrimPathContent) {
        contents.add(trimPath)
    }

    fun apply(path: Path) {
        for (i in contents.indices.reversed()) {
            applyTrimPathIfNeeded(path, contents[i])
        }
    }
}
