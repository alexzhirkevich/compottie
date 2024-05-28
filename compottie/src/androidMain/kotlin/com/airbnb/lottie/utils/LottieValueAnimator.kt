package com.airbnb.lottie.utils

import android.animation.ValueAnimator
import android.view.Choreographer
import androidx.annotation.FloatRange
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.airbnb.lottie.LottieComposition
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * This is a slightly modified [ValueAnimator] that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
class LottieValueAnimator : BaseLottieAnimator(), Choreographer.FrameCallback {
    /**
     * Returns the current speed. This will be affected by repeat mode REVERSE.
     */
    @JvmField
    var speed: Float = 1f
    private var speedReversedForRepeatMode = false
    private var lastFrameTimeNs: Long = 0
    private var frameRaw = 0f
    private var frame = 0f
    private var repeatCount = 0
    private var minFrame = Int.MIN_VALUE.toFloat()
    private var maxFrame = Int.MAX_VALUE.toFloat()
    private var composition: LottieComposition? = null

    @VisibleForTesting
    protected var running: Boolean = false
    private var useCompositionFrameRate = false

    /**
     * Returns a float representing the current value of the animation from 0 to 1
     * regardless of the animation speed, direction, or min and max frames.
     */
    override fun getAnimatedValue(): Any {
        return animatedValueAbsolute
    }

    val animatedValueAbsolute: Float
        /**
         * Returns the current value of the animation from 0 to 1 regardless
         * of the animation speed, direction, or min and max frames.
         */
        get() {
            if (composition == null) {
                return 0f
            }
            return (frame - composition!!.startFrame) / (composition!!.endFrame - composition!!.startFrame)
        }

    /**
     * Returns the current value of the currently playing animation taking into
     * account direction, min and max frames.
     */
    override fun getAnimatedFraction(): Float {
        if (composition == null) {
            return 0f
        }
        return if (isReversed) {
            (getMaxFrame() - frame) / (getMaxFrame() - getMinFrame())
        } else {
            (frame - getMinFrame()) / (getMaxFrame() - getMinFrame())
        }
    }

    override fun getDuration(): Long {
        return if (composition == null) 0 else composition!!.duration.toLong()
    }

    fun getFrame(): Float {
        return frame
    }

    override fun isRunning(): Boolean {
        return running
    }

    fun setUseCompositionFrameRate(useCompositionFrameRate: Boolean) {
        this.useCompositionFrameRate = useCompositionFrameRate
    }

    override fun doFrame(frameTimeNanos: Long) {
        postFrameCallback()
        if (composition == null || !isRunning) {
            return
        }

        val timeSinceFrame = if (lastFrameTimeNs == 0L) 0 else frameTimeNanos - lastFrameTimeNs
        val frameDuration = frameDurationNs
        val dFrames = timeSinceFrame / frameDuration

        val newFrameRaw = frameRaw + (if (isReversed) -dFrames else dFrames)
        val ended = !MiscUtils.contains(newFrameRaw, getMinFrame(), getMaxFrame())
        val previousFrameRaw = frameRaw
        frameRaw = MiscUtils.clamp(newFrameRaw, getMinFrame(), getMaxFrame())
        frame = if (useCompositionFrameRate) floor(frameRaw.toDouble()).toFloat() else frameRaw

        lastFrameTimeNs = frameTimeNanos

        if (!useCompositionFrameRate || frameRaw != previousFrameRaw) {
            notifyUpdate()
        }
        if (ended) {
            if (getRepeatCount() != ValueAnimator.INFINITE && repeatCount >= getRepeatCount()) {
                frameRaw = if (speed < 0) getMinFrame() else getMaxFrame()
                frame = frameRaw
                removeFrameCallback()
                notifyEnd(isReversed)
            } else {
                notifyRepeat()
                repeatCount++
                if (repeatMode == ValueAnimator.REVERSE) {
                    speedReversedForRepeatMode = !speedReversedForRepeatMode
                    reverseAnimationSpeed()
                } else {
                    frameRaw = if (isReversed) getMaxFrame() else getMinFrame()
                    frame = frameRaw
                }
                lastFrameTimeNs = frameTimeNanos
            }
        }

        verifyFrame()
    }

    private val frameDurationNs: Float
        get() {
            if (composition == null) {
                return Float.MAX_VALUE
            }
            return (Utils.SECOND_IN_NANOS / composition!!.frameRate / abs(
                speed.toDouble()
            )).toFloat()
        }

    fun clearComposition() {
        this.composition = null
        minFrame = Int.MIN_VALUE.toFloat()
        maxFrame = Int.MAX_VALUE.toFloat()
    }

    fun setComposition(composition: LottieComposition) {
        // Because the initial composition is loaded async, the first min/max frame may be set
        val keepMinAndMaxFrames = this.composition == null
        this.composition = composition

        if (keepMinAndMaxFrames) {
            setMinAndMaxFrames(
                max(minFrame.toDouble(), composition.startFrame.toDouble())
                    .toFloat(),
                min(maxFrame.toDouble(), composition.endFrame.toDouble())
                    .toFloat()
            )
        } else {
            setMinAndMaxFrames(
                composition.startFrame.toInt().toFloat(), composition.endFrame.toInt().toFloat()
            )
        }
        val frame = this.frame
        this.frame = 0f
        this.frameRaw = 0f
        setFrame(frame.toInt().toFloat())
        notifyUpdate()
    }

    fun setFrame(frame: Float) {
        if (this.frameRaw == frame) {
            return
        }
        this.frameRaw = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame())
        this.frame = if (useCompositionFrameRate) (floor(frameRaw.toDouble())
            .toFloat()) else frameRaw
        lastFrameTimeNs = 0
        notifyUpdate()
    }

    fun setMinFrame(minFrame: Int) {
        setMinAndMaxFrames(minFrame.toFloat(), maxFrame.toInt().toFloat())
    }

    fun setMaxFrame(maxFrame: Float) {
        setMinAndMaxFrames(minFrame, maxFrame)
    }

    fun setMinAndMaxFrames(minFrame: Float, maxFrame: Float) {
        require(!(minFrame > maxFrame)) {
            String.format(
                "minFrame (%s) must be <= maxFrame (%s)",
                minFrame,
                maxFrame
            )
        }
        val compositionMinFrame =
            if (composition == null) -Float.MAX_VALUE else composition!!.startFrame
        val compositionMaxFrame =
            if (composition == null) Float.MAX_VALUE else composition!!.endFrame
        val newMinFrame = MiscUtils.clamp(minFrame, compositionMinFrame, compositionMaxFrame)
        val newMaxFrame = MiscUtils.clamp(maxFrame, compositionMinFrame, compositionMaxFrame)
        if (newMinFrame != this.minFrame || newMaxFrame != this.maxFrame) {
            this.minFrame = newMinFrame
            this.maxFrame = newMaxFrame
            setFrame(MiscUtils.clamp(frame, newMinFrame, newMaxFrame).toInt().toFloat())
        }
    }

    fun reverseAnimationSpeed() {
        speed = -speed
    }

    override fun setRepeatMode(value: Int) {
        super.setRepeatMode(value)
        if (value != ValueAnimator.REVERSE && speedReversedForRepeatMode) {
            speedReversedForRepeatMode = false
            reverseAnimationSpeed()
        }
    }

    @MainThread
    fun playAnimation() {
        running = true
        notifyStart(isReversed)
        setFrame((if (isReversed) getMaxFrame() else getMinFrame()).toInt().toFloat())
        lastFrameTimeNs = 0
        repeatCount = 0
        postFrameCallback()
    }

    @MainThread
    fun endAnimation() {
        removeFrameCallback()
        notifyEnd(isReversed)
    }

    @MainThread
    fun pauseAnimation() {
        removeFrameCallback()
        notifyPause()
    }

    @MainThread
    fun resumeAnimation() {
        running = true
        postFrameCallback()
        lastFrameTimeNs = 0
        if (isReversed && getFrame() == getMinFrame()) {
            setFrame(getMaxFrame())
        } else if (!isReversed && getFrame() == getMaxFrame()) {
            setFrame(getMinFrame())
        }
        notifyResume()
    }

    @MainThread
    override fun cancel() {
        notifyCancel()
        removeFrameCallback()
    }

    private val isReversed: Boolean
        get() = speed < 0

    fun getMinFrame(): Float {
        if (composition == null) {
            return 0f
        }
        return if (minFrame.toInt() == Int.MIN_VALUE) composition!!.startFrame else minFrame
    }

    fun getMaxFrame(): Float {
        if (composition == null) {
            return 0f
        }
        return if (maxFrame.toInt() == Int.MAX_VALUE) composition!!.endFrame else maxFrame
    }

    override fun notifyCancel() {
        super.notifyCancel()
        notifyEnd(isReversed)
    }

    protected fun postFrameCallback() {
        if (isRunning) {
            removeFrameCallback(false)
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    @MainThread
    protected fun removeFrameCallback() {
        this.removeFrameCallback(true)
    }

    @MainThread
    protected fun removeFrameCallback(stopRunning: Boolean) {
        Choreographer.getInstance().removeFrameCallback(this)
        if (stopRunning) {
            running = false
        }
    }

    private fun verifyFrame() {
        if (composition == null) {
            return
        }
        check(!(frame < minFrame || frame > maxFrame)) {
            String.format(
                "Frame must be [%f,%f]. It is %f",
                minFrame,
                maxFrame,
                frame
            )
        }
    }
}
