package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextJustify

interface DynamicTextLayer: DynamicLayer {

    /**
     * Dynamically provide text content for this layer.
     */
    fun text(provider: PropertyProvider<String>)

    fun fontSize(provider: PropertyProvider<Float>)

    fun lineHeight(provider: PropertyProvider<Float>)

    fun lineSpacing(provider: PropertyProvider<Float>)

    fun fillColor(provider: PropertyProvider<Color>)

    fun strokeColor(provider: PropertyProvider<Color>)

    fun strokeWidth(provider: PropertyProvider<Float>)

    /**
     * If stroke should be drawn before fill
     * */
    fun strokeOverFill(provider: PropertyProvider<Boolean>)

    fun textJustify(provider: PropertyProvider<TextJustify>)

    fun baselineShift(provider: PropertyProvider<Float>)

    fun tracking(provider: PropertyProvider<Float>)

    fun size(provider: PropertyProvider<Size>)

    fun position(provider: PropertyProvider<Offset>)
}

