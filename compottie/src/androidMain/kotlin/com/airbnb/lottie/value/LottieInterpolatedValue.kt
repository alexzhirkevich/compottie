package com.airbnb.lottie.value

abstract class LottieInterpolatedValue<T> @JvmOverloads constructor(
    private val startValue: T,
    private val endValue: T,
    private val interpolator: Interpolator = Interpolator.Linear
) : LottieValueCallback<T>() {

    override fun getValue(frameInfo: LottieFrameInfo<T>): T {
        val progress = interpolator.getInterpolation(frameInfo.overallProgress)
        return interpolateValue(this.startValue, this.endValue, progress)
    }

    abstract fun interpolateValue(startValue: T, endValue: T, progress: Float): T
}
