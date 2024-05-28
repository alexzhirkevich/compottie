package com.airbnb.lottie.animation.keyframe

import android.annotation.SuppressLint
import androidx.annotation.FloatRange
import com.airbnb.lottie.L
import com.airbnb.lottie.value.Keyframe
import com.airbnb.lottie.value.LottieValueCallback

/**
 * @param <K> Keyframe type
 * @param <A> Animation type
</A></K> */
@SuppressLint("RestrictedApi")
abstract class BaseKeyframeAnimation<K, A> internal constructor(keyframes: List<Keyframe<K>>) {
    interface AnimationListener {
        fun onValueChanged()
    }

    // This is not a Set because we don't want to create an iterator object on every setProgress.
    val listeners: MutableList<AnimationListener> = ArrayList(1)
    private var isDiscrete = false

    private val keyframesWrapper: KeyframesWrapper<K> = wrap(keyframes)
    protected var progress: Float = 0f
    protected var valueCallback: LottieValueCallback<A>? = null

    private var cachedGetValue: A? = null

    private var cachedStartDelayProgress = -1f
    private var cachedEndProgress = -1f

    fun setIsDiscrete() {
        isDiscrete = true
    }

    fun addUpdateListener(listener: AnimationListener) {
        listeners.add(listener)
    }

    open fun setProgress(/*@FloatRange(from = 0f, to = 1f)*/ progress: Float) {
        var progress = progress
        if (keyframesWrapper.isEmpty) {
            return
        }
        if (progress < startDelayProgress) {
            progress = startDelayProgress
        } else if (progress > endProgress) {
            progress = endProgress
        }

        if (progress == this.progress) {
            return
        }
        this.progress = progress
        if (keyframesWrapper.isValueChanged(progress)) {
            notifyListeners()
        }
    }

    open fun notifyListeners() {
        for (i in listeners.indices) {
            listeners[i].onValueChanged()
        }
    }

    val currentKeyframe: Keyframe<K>
        get() {
            val keyframe: Keyframe<K> = keyframesWrapper.currentKeyframe
            return keyframe
        }

    val linearCurrentKeyframeProgress: Float
        /**
         * Returns the progress into the current keyframe between 0 and 1. This does not take into account
         * any interpolation that the keyframe may have.
         */
        get() {
            if (isDiscrete) {
                return 0f
            }

            val keyframe = currentKeyframe
            if (keyframe.isStatic) {
                return 0f
            }
            val progressIntoFrame = progress - keyframe.startProgress
            val keyframeProgress = keyframe.endProgress - keyframe.startProgress
            return progressIntoFrame / keyframeProgress
        }

    val interpolatedCurrentKeyframeProgress: Float
        /**
         * Takes the value of [.getLinearCurrentKeyframeProgress] and interpolates it with
         * the current keyframe's interpolator.
         */
        get() {
            val keyframe = currentKeyframe
            // Keyframe should not be null here but there seems to be a Xiaomi Android 10 specific crash.
            // https://github.com/airbnb/lottie-android/issues/2050
            // https://github.com/airbnb/lottie-android/issues/2483
            if (keyframe == null || keyframe.isStatic || keyframe.interpolator == null) {
                return 0f
            }
            return keyframe.interpolator.getInterpolation(linearCurrentKeyframeProgress)
        }

//    @get:FloatRange(from = 0f, to = 1f)
    @get:SuppressLint("Range")
    private val startDelayProgress: Float
        get() {
            if (cachedStartDelayProgress == -1f) {
                cachedStartDelayProgress = keyframesWrapper.startDelayProgress
            }
            return cachedStartDelayProgress
        }

//    @get:FloatRange(from = 0f, to = 1f)
    @get:SuppressLint("Range")
    open val endProgress: Float
        get() {
            if (cachedEndProgress == -1f) {
                cachedEndProgress = keyframesWrapper.endProgress
            }
            return cachedEndProgress
        }

    open val value: A?
        get() {
            val value: A

            val linearProgress = linearCurrentKeyframeProgress
            if (valueCallback == null && keyframesWrapper.isCachedValueEnabled(linearProgress)) {
                return cachedGetValue
            }
            val keyframe = currentKeyframe

            if (keyframe.xInterpolator != null && keyframe.yInterpolator != null) {
                val xProgress = keyframe.xInterpolator.getInterpolation(linearProgress)
                val yProgress = keyframe.yInterpolator.getInterpolation(linearProgress)
                value = getValue(keyframe, linearProgress, xProgress, yProgress)
            } else {
                val progress = interpolatedCurrentKeyframeProgress
                value = getValue(keyframe, progress)
            }

            cachedGetValue = value
            return value
        }

    fun getProgress(): Float {
        return progress
    }

    fun setValueCallback(valueCallback: LottieValueCallback<A>?) {
        if (this.valueCallback != null) {
            this.valueCallback!!.setAnimation(null)
        }
        this.valueCallback = valueCallback
        valueCallback?.setAnimation(this)
    }

    fun hasValueCallback(): Boolean {
        return valueCallback != null
    }

    /**
     * keyframeProgress will be [0, 1] unless the interpolator has overshoot in which case, this
     * should be able to handle values outside of that range.
     */
    abstract fun getValue(keyframe: Keyframe<K>?, keyframeProgress: Float): A

    /**
     * Similar to [.getValue] but used when an animation has separate interpolators for the X and Y axis.
     */
    protected open fun getValue(
        keyframe: Keyframe<K>?,
        linearKeyframeProgress: Float,
        xKeyframeProgress: Float,
        yKeyframeProgress: Float
    ): A {
        throw UnsupportedOperationException("This animation does not support split dimensions!")
    }

    private interface KeyframesWrapper<T> {
        val isEmpty: Boolean

        fun isValueChanged(progress: Float): Boolean

        val currentKeyframe: Keyframe<T>

//        @get:FloatRange(from = 0f, to = 1f)
        val startDelayProgress: Float

//        @get:FloatRange(from = 0f, to = 1f)
        val endProgress: Float

        fun isCachedValueEnabled(progress: Float): Boolean
    }

    private class EmptyKeyframeWrapper<T> : KeyframesWrapper<T> {
        override val isEmpty: Boolean
            get() = true

        override fun isValueChanged(progress: Float): Boolean {
            return false
        }

        override val currentKeyframe: Keyframe<T>
            get() {
                throw IllegalStateException("not implemented")
            }

        override val startDelayProgress: Float
            get() = 0f

        override val endProgress: Float
            get() = 1f

        override fun isCachedValueEnabled(progress: Float): Boolean {
            throw IllegalStateException("not implemented")
        }
    }

    private class SingleKeyframeWrapper<T>(keyframes: List<Keyframe<T>>) : KeyframesWrapper<T> {
        override val currentKeyframe: Keyframe<T> = keyframes[0]
        private var cachedInterpolatedProgress = -1f

        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            return !currentKeyframe.isStatic
        }

        override val startDelayProgress: Float
            get() = currentKeyframe.startProgress

        override val endProgress: Float
            get() = currentKeyframe.endProgress

        override fun isCachedValueEnabled(progress: Float): Boolean {
            if (cachedInterpolatedProgress == progress) {
                return true
            }
            cachedInterpolatedProgress = progress
            return false
        }
    }

    private class KeyframesWrapperImpl<T>(private val keyframes: List<Keyframe<T>>) :
        KeyframesWrapper<T> {
        override var currentKeyframe: Keyframe<T>
            private set
        private var cachedCurrentKeyframe: Keyframe<T>? = null
        private var cachedInterpolatedProgress = -1f

        init {
            currentKeyframe = findKeyframe(0f)
        }

        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            if (currentKeyframe.containsProgress(progress)) {
                return !currentKeyframe.isStatic
            }
            currentKeyframe = findKeyframe(progress)
            return true
        }

        private fun findKeyframe(progress: Float): Keyframe<T> {
            var keyframe = keyframes[keyframes.size - 1]
            if (progress >= keyframe.startProgress) {
                return keyframe
            }
            for (i in keyframes.size - 2 downTo 1) {
                keyframe = keyframes[i]
                if (currentKeyframe === keyframe) {
                    continue
                }
                if (keyframe.containsProgress(progress)) {
                    return keyframe
                }
            }
            return keyframes[0]
        }

        override val startDelayProgress: Float
            get() = keyframes[0].startProgress

        override val endProgress: Float
            get() = keyframes[keyframes.size - 1].endProgress

        override fun isCachedValueEnabled(progress: Float): Boolean {
            if (cachedCurrentKeyframe === currentKeyframe
                && cachedInterpolatedProgress == progress
            ) {
                return true
            }
            cachedCurrentKeyframe = currentKeyframe
            cachedInterpolatedProgress = progress
            return false
        }
    }

    companion object {
        private fun <T> wrap(keyframes: List<Keyframe<T>>): KeyframesWrapper<T> {
            if (keyframes.isEmpty()) {
                return EmptyKeyframeWrapper()
            }
            if (keyframes.size == 1) {
                return SingleKeyframeWrapper(keyframes)
            }
            return KeyframesWrapperImpl(keyframes)
        }
    }
}
