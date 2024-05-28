package com.airbnb.lottie.value

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp


@Suppress("unused")
class LottieInterpolatedPointValue(
    startValue: Offset,
    endValue: Offset,
    interpolator: Interpolator = Interpolator.Linear
) : LottieInterpolatedValue<Offset>(startValue, endValue, interpolator) {

    public override fun interpolateValue(
        startValue: Offset,
        endValue: Offset,
        progress: Float
    ): Offset {
        return lerp(startValue, endValue, progress)
    }
}
