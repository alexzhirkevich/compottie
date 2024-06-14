package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter


internal sealed interface DynamicDrawProvider : DynamicDraw {
    val opacity: PropertyProvider<Float>?
    val colorFilter: PropertyProvider<ColorFilter?>?
    val blendMode: PropertyProvider<BlendMode>?
}

internal sealed interface DynamicSolidDrawProvider : DynamicDrawProvider, DynamicDraw.Solid {
    val color: PropertyProvider<Color>?
}

internal sealed interface DynamicGradientDrawProvider : DynamicDrawProvider, DynamicDraw.Gradient {
    val gradient: GradientProvider?
}

internal fun DynamicGradientDrawProvider() : DynamicGradientDrawProvider = DynamicGradientDrawProviderImpl()

internal fun DynamicSolidDrawProvider() : DynamicSolidDrawProvider = DynamicSolidDrawProviderImpl()


internal sealed class BaseDynamicDrawProvider : DynamicDraw, DynamicDrawProvider {

    final override var opacity: PropertyProvider<Float>? = null
        private set

    final override var colorFilter: PropertyProvider<ColorFilter?>? = null
        private set

    final override var blendMode: PropertyProvider<BlendMode>? = null
        private set

    final override fun opacity(provider: PropertyProvider<Float>) {
        opacity = provider
    }

    final override fun colorFilter(provider: PropertyProvider<ColorFilter?>) {
        colorFilter = provider
    }

    final override fun blendMode(provider: PropertyProvider<BlendMode>) {
        blendMode = provider
    }
}

private class DynamicSolidDrawProviderImpl : BaseDynamicDrawProvider(),DynamicSolidDrawProvider, DynamicDraw.Solid {
    override var color: PropertyProvider<Color>? = null
        private set

    override fun color(provider: PropertyProvider<Color>) {
        color = provider
    }
}

private class DynamicGradientDrawProviderImpl :
    BaseDynamicDrawProvider(), DynamicGradientDrawProvider, DynamicDraw.Gradient {

    override var gradient: GradientProvider? = null
        private set

    override fun gradient(provider: GradientProvider) {
        gradient = provider
    }
}