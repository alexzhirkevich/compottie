package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.keight.js.JsAny

internal interface PropertyGroup : JsAny {
    val group : PropertyGroup?
}