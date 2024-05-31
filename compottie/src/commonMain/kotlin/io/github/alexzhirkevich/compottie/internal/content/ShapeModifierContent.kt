package io.github.alexzhirkevich.compottie.internal.content

import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.schema.helpers.ShapeData

internal interface ShapeModifierContent : Content {
    fun modify(shape: ShapeData, frame: Float) : ShapeData
}


internal fun ShapeData.modifiedBy(modifiers: List<ShapeModifierContent>, frame: Float) : ShapeData {
    var b = this

    modifiers.fastForEachReversed {
        b = it.modify(b, frame)
    }

    return b
}