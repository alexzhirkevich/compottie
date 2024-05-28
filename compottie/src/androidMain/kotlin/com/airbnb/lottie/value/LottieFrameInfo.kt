package com.airbnb.lottie.value

import androidx.annotation.RestrictTo

/**
 * Data class for use with [LottieValueCallback].
 * You should *not* hold a reference to the frame info parameter passed to your callback. It will be reused.
 */
class LottieFrameInfo<T>(
    var startFrame: Float = 0f,
    var endFrame: Float = 0f,
    var startValue: T,
    var endValue: T,
    var linearKeyframeProgress: Float = 0f,
    var interpolatedKeyframeProgress: Float = 0f,
    var overallProgress: Float = 0f
)