package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

sealed interface DynamicFill : DynamicShape {
    fun color(provider : PropertyProvider<Color>)

    fun opacity(provider : PropertyProvider<Float>)

    fun colorFilter(provider : PropertyProvider<ColorFilter?>)

    fun blendMode(provider: PropertyProvider<BlendMode>)
}