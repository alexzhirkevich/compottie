package com.airbnb.lottie.value

import androidx.compose.ui.util.lerp

@Suppress("unused")
class LottieInterpolatedIntegerValue(
    startValue: Int, endValue: Int, interpolator: Interpolator
) : LottieInterpolatedValue<Int>(startValue, endValue, interpolator) {

    public override fun interpolateValue(startValue: Int, endValue: Int, progress: Float): Int {
        return lerp(startValue, endValue, progress)
    }
}
