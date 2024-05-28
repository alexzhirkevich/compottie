package com.airbnb.lottie.animation.keyframe

import android.annotation.SuppressLint
import com.airbnb.lottie.utils.GammaEvaluator
import com.airbnb.lottie.utils.MiscUtils
import com.airbnb.lottie.value.Keyframe

@SuppressLint("RestrictedApi")
internal class ColorKeyframeAnimation(keyframes: List<Keyframe<Int>>) : KeyframeAnimation<Int>(keyframes) {
    override fun getValue(keyframe: Keyframe<Int>?, keyframeProgress: Float): Int {
        return getIntValue(keyframe, keyframeProgress)
    }

    /**
     * Optimization to avoid autoboxing.
     */
    fun getIntValue(keyframe: Keyframe<Int>?, keyframeProgress: Float): Int {
        check(!(keyframe!!.startValue == null || keyframe.endValue == null)) { "Missing values for keyframe." }

        // keyframe.endFrame should not be null under normal operation.
        // It is not clear why this would be null and when it does, it seems to be extremely rare.
        // https://github.com/airbnb/lottie-android/issues/2361
        if (valueCallback != null && keyframe.endFrame != null) {
            val value = valueCallback!!.getValueInternal(
                keyframe.startFrame,
                keyframe.endFrame!!,
                keyframe.startValue,
                keyframe.endValue,
                keyframeProgress,
                linearCurrentKeyframeProgress,
                getProgress()
            )
            if (value != null) {
                return value
            }
        }

        return GammaEvaluator.evaluate(
            MiscUtils.clamp(keyframeProgress, 0f, 1f),
            keyframe.startValue,
            keyframe.endValue!!
        )
    }

    val intValue: Int
        /**
         * Optimization to avoid autoboxing.
         */
        get() = getIntValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress())
}
