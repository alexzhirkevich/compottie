package com.airbnb.lottie.utils

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Build
import java.util.concurrent.CopyOnWriteArraySet

abstract class BaseLottieAnimator : ValueAnimator() {
    private val updateListeners: MutableSet<AnimatorUpdateListener> = CopyOnWriteArraySet()
    private val listeners: MutableSet<AnimatorListener> = CopyOnWriteArraySet()
    private val pauseListeners: MutableSet<AnimatorPauseListener> = CopyOnWriteArraySet()


    override fun getStartDelay(): Long {
        throw UnsupportedOperationException("LottieAnimator does not support getStartDelay.")
    }

    override fun setStartDelay(startDelay: Long) {
        throw UnsupportedOperationException("LottieAnimator does not support setStartDelay.")
    }

    override fun setDuration(duration: Long): ValueAnimator {
        throw UnsupportedOperationException("LottieAnimator does not support setDuration.")
    }

    override fun setInterpolator(value: TimeInterpolator) {
        throw UnsupportedOperationException("LottieAnimator does not support setInterpolator.")
    }

    override fun addUpdateListener(listener: AnimatorUpdateListener) {
        updateListeners.add(listener)
    }

    override fun removeUpdateListener(listener: AnimatorUpdateListener) {
        updateListeners.remove(listener)
    }

    override fun removeAllUpdateListeners() {
        updateListeners.clear()
    }

    override fun addListener(listener: AnimatorListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AnimatorListener) {
        listeners.remove(listener)
    }

    override fun removeAllListeners() {
        listeners.clear()
    }

    fun notifyStart(isReverse: Boolean) {
        for (listener in listeners) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                listener.onAnimationStart(this, isReverse)
            } else {
                listener.onAnimationStart(this)
            }
        }
    }

    override fun addPauseListener(listener: AnimatorPauseListener) {
        pauseListeners.add(listener)
    }

    override fun removePauseListener(listener: AnimatorPauseListener) {
        pauseListeners.remove(listener)
    }

    fun notifyRepeat() {
        for (listener in listeners) {
            listener.onAnimationRepeat(this)
        }
    }

    fun notifyEnd(isReverse: Boolean) {
        for (listener in listeners) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                listener.onAnimationEnd(this, isReverse)
            } else {
                listener.onAnimationEnd(this)
            }
        }
    }

    open fun notifyCancel() {
        for (listener in listeners) {
            listener.onAnimationCancel(this)
        }
    }

    fun notifyUpdate() {
        for (listener in updateListeners) {
            listener.onAnimationUpdate(this)
        }
    }

    fun notifyPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (pauseListener in pauseListeners) {
                pauseListener.onAnimationPause(this)
            }
        }
    }

    fun notifyResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (pauseListener in pauseListeners) {
                pauseListener.onAnimationResume(this)
            }
        }
    }
}
