package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.schema.helpers.Bezier

internal interface ShapeModifierContent {
    fun modify(shape: Bezier) : Bezier
}


internal fun Bezier.modifiedBy(modifiers : List<ShapeModifierContent>) : Bezier {
    var b = this

    modifiers.fastForEachReversed {
        b = it.modify(b)
    }

    return b
}