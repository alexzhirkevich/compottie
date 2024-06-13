package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ScaleFactor

interface DynamicTransform {

    fun scale(provider : PropertyProvider<ScaleFactor>)

    fun offset(provider : PropertyProvider<Offset>)

    fun rotation(provider : PropertyProvider<Float>)

    fun opacity(provider : PropertyProvider<Float>)

    fun skew(provider : PropertyProvider<Float>)

    fun skewAxis(provider : PropertyProvider<Float>)
}