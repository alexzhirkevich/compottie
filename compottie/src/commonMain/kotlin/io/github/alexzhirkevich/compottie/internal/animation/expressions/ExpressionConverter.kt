package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js

internal fun Vec2.js() = listOf(x.js(), y.js()).js()
internal fun Color.js() = listOf(red.js(), green.js(), blue.js(), alpha.js()).js()

internal fun Any.toJs() : JsAny {
    return when(this){
        is JsAny -> this
        is Vec2 -> js()
        is Color -> js()
        is Number -> js()
        is CharSequence -> js()
        is List<*> -> fastMap { it?.toJs() }.js()
        else -> Undefined
    }
}