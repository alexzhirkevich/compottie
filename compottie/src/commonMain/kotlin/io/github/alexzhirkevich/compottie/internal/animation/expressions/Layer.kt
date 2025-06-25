package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastFirstOrNull
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.js.Undefined

internal fun JSGetLayerEffect(layer: Layer) = Callable {
    val index = it[0]?.toKotlin(this) ?: return@Callable Undefined

    if (index is Number) {
        val i = index.toInt()
        layer.effects.fastFirstOrNull { e -> e.index == i }
    } else {
        val n = index.toString()
        layer.effects.fastFirstOrNull { e -> e.name == n || e.matchName == n }
    }
}
