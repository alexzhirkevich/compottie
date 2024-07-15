package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.avp.xml.AnimatedVectorProperty

@Composable
public fun rememberNumberAnimator(
    duration: Float,
    valueFrom: Float,
    valueTo: Float,
    property: AnimatedVectorProperty<FloatAnimator>,
    delay : Float = 0f,
    interpolator: Easing = LinearEasing,
) : FloatAnimator {
    return remember(duration, valueFrom, valueTo, property, delay, interpolator) {
        FloatAnimator(duration, valueFrom, valueTo, property, delay, interpolator)
    }
}
