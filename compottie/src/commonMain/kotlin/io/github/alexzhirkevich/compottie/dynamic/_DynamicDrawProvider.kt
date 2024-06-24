package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.platform.GradientCache
import io.github.alexzhirkevich.compottie.internal.platform.GradientShader

internal sealed interface DynamicDrawProvider : DynamicDraw {
    val opacity: PropertyProvider<Float>?
    val colorFilter: PropertyProvider<ColorFilter?>?
    val blendMode: PropertyProvider<BlendMode>?

    val hidden : PropertyProvider<Boolean>?

    val gradient: GradientProvider?
    val color: PropertyProvider<Color>?
}

internal sealed class BaseDynamicDrawProvider : DynamicShapeProvider(),
    DynamicDraw, DynamicDrawProvider{

    final override var opacity: PropertyProvider<Float>? = null
        private set

    final override var colorFilter: PropertyProvider<ColorFilter?>? = null
        private set

    final override var blendMode: PropertyProvider<BlendMode>? = null
        private set

    final override var gradient: GradientProvider? = null
        private set

    final override var color: PropertyProvider<Color>? = null
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

    override fun gradient(provider: GradientProvider) {
        gradient = provider
        color = null
    }
    override fun color(provider: PropertyProvider<Color>) {
        color = provider
        gradient = null
    }
}

internal fun DynamicDrawProvider?.applyToPaint(
    paint: Paint,
    state: AnimationState,
    parentAlpha: Float,
    parentMatrix : Matrix,
    opacity : AnimatedNumber?,
    size: () -> Rect,
    gradientCache: GradientCache
) {
    this?.color?.let {
        paint.color = it.derive(paint.color, state)
    }
    this?.gradient?.let {
        paint.shader = GradientShader(
            gradient = it.invoke(state, size()),
            matrix = parentMatrix,
            cache = gradientCache
        )
    }

    var alpha = 1f

    opacity?.interpolatedNorm(state)?.let {
        alpha = (alpha * it).coerceIn(0f,1f)
    }

    this?.opacity?.let {
        alpha = it.derive(alpha, state).coerceIn(0f,1f)
    }

    paint.alpha = (alpha * parentAlpha).coerceIn(0f,1f)
    paint.colorFilter = this?.colorFilter.derive(paint.colorFilter, state)
    paint.blendMode = this?.blendMode.derive(paint.blendMode, state)
}


