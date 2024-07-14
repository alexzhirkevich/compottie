package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.util.lerp

internal sealed class FloatAnimator : ObjectAnimator<Float, Float>()

internal class DynamicFloatAnimator(
    override val duration: Float,
    override val valueFrom: Float,
    override val valueTo: Float,
    override val startOffset: Float,
    override val interpolator: Easing
) : FloatAnimator() {

    override fun interpolate(progress: Float): Float {
        return lerp(valueFrom, valueTo, progress)
    }
}

internal class StaticFloatAnimator(
    private val value : Float
) : FloatAnimator() {
    override val startOffset: Float get() = 0f
    override val duration: Float get() = 0f
    override val valueFrom: Float get() = value
    override val valueTo: Float get() = value
    override val interpolator: Easing get() = LinearEasing

    override fun interpolate(progress: Float): Float {
        return value
    }
}

internal fun Float.toAnimator() = StaticFloatAnimator(this)