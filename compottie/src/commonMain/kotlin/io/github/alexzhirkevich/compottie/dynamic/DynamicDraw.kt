package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import io.github.alexzhirkevich.compottie.internal.AnimationState

public sealed interface DynamicDraw : DynamicShape {

    public fun opacity(provider: PropertyProvider<Float>)

    public fun colorFilter(provider: PropertyProvider<ColorFilter?>)

    public fun blendMode(provider: PropertyProvider<BlendMode>)

    public fun color(provider: PropertyProvider<Color>)

    /**
     * Dynamic gradient provider.
     *
     * Will be invoked for each frame therefore gradient instance should be cached if possible
     *
     * Example:
     *
     * ```kotlin
     * gradient { bounds ->
     *     LottieGradient.Linear(
     *          colorStops = listOf(0f to Color.Red, 1f to Color.Blue),
     *          start = bounds.topLeft,
     *          end = bounds.bottomRight
     *      )
     * }
     * ```
     * */
    public fun gradient(provider: GradientProvider)
}

public typealias GradientProvider = AnimationState.(Rect) -> LottieGradient

public sealed interface LottieGradient {

    public data class Linear(
        val colorStops : List<Pair<Float, Color>>,
        val start : Offset = Offset.Unspecified,
        val end: Offset = Offset.Unspecified,
    ) : LottieGradient

    public data class Radial(
        val colorStops : List<Pair<Float, Color>>,
        val center : Offset = Offset.Unspecified,
        val radius : Float = Float.NaN,
    ) : LottieGradient
}