package com.airbnb.lottie.value

import androidx.compose.ui.util.lerp

@Suppress("unused")
class LottieInterpolatedFloatValue(
    startValue: Float,
    endValue: Float,
    interpolator: Interpolator = Interpolator.Linear
) : LottieInterpolatedValue<Float>(startValue, endValue, interpolator) {

    override fun interpolateValue(
        startValue: Float,
        endValue: Float,
        progress: Float
    ): Float {
        return lerp(startValue, endValue, progress)
    }
}
