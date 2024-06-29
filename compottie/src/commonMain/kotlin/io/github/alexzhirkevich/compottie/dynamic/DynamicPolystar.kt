package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlinx.serialization.SerialName

interface DynamicPolystar : DynamicShape {

    fun size(provider : PropertyProvider<Size>)

    fun position(provider : PropertyProvider<Offset>)

    fun rotation(provider : PropertyProvider<Float>)

    fun points(provider : PropertyProvider<Float>)

    fun innerRoundness(provider : PropertyProvider<Float>)

    fun innerRadius(provider : PropertyProvider<Float>)

    fun outerRadius(provider : PropertyProvider<Float>)

    fun outerRoundness(provider : PropertyProvider<Float>)
}