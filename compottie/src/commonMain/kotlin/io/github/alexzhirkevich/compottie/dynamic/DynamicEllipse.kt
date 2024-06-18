package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

interface DynamicEllipse : DynamicShape {

    fun size(provider : PropertyProvider<Size>)

    fun position(provider : PropertyProvider<Offset>)
}