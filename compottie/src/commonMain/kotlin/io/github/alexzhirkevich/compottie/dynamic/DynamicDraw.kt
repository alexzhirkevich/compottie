package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import io.github.alexzhirkevich.compottie.internal.AnimationState

sealed interface DynamicDraw : DynamicShape {

    fun opacity(provider: PropertyProvider<Float>)

    fun colorFilter(provider: PropertyProvider<ColorFilter?>)

    fun blendMode(provider: PropertyProvider<BlendMode>)

    fun color(provider: PropertyProvider<Color>)

    fun gradient(provider: GradientProvider)
}

typealias GradientProvider = (Size, AnimationState) -> LottieGradient

sealed interface LottieGradient {

    data class Linear(
        val colorStops : List<Pair<Float, Color>>,
        val start : Offset = Offset.Unspecified,
        val end: Offset = Offset.Unspecified,
    ) : LottieGradient

    data class Radial(
        val colorStops : List<Pair<Float, Color>>,
        val center : Offset = Offset.Unspecified,
        val radius : Float = Float.NaN,
    ) : LottieGradient
}