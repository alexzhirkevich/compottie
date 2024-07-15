package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.avp.xml.AnimatedVectorProperty

public sealed class FloatAnimator : ObjectAnimator<Float, Float>()

public fun FloatAnimator(
    duration: Float,
    valueFrom: Float,
    valueTo: Float,
    property: AnimatedVectorProperty<FloatAnimator>,
    delay : Float = 0f,
    interpolator: Easing = LinearEasing,
) : FloatAnimator = DynamicFloatAnimator(
    duration = duration,
    valueFrom = valueFrom,
    valueTo = valueTo,
    delay = delay,
    easing = interpolator,
    property = property
)

internal class DynamicFloatAnimator(
    override val duration: Float,
    override val valueFrom: Float,
    override val valueTo: Float,
    override val delay: Float,
    override val easing: Easing,
    override val property: AnimatedVectorProperty<FloatAnimator>
) : FloatAnimator() {

    override fun interpolate(progress: Float): Float {
        return lerp(valueFrom, valueTo, progress)
    }
}

internal class StaticFloatAnimator(
    private val value : Float,
    override val property: AnimatedVectorProperty<FloatAnimator>
) : FloatAnimator() {
    override val delay: Float get() = 0f
    override val duration: Float get() = 0f
    override val valueFrom: Float get() = value
    override val valueTo: Float get() = value
    override val easing: Easing get() = LinearEasing

    override fun interpolate(progress: Float): Float {
        return value
    }
}
