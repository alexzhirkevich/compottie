package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter

interface DrawShape {

    fun opacity(provider : PropertyProvider<Float>)

    fun colorFilter(provider : PropertyProvider<ColorFilter?>)

    fun blendMode(provider: PropertyProvider<BlendMode>)
}