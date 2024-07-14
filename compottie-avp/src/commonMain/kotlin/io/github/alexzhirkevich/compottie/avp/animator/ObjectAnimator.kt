package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing

internal abstract class ObjectAnimator<T, out R> {

    abstract val startOffset: Float

    abstract val duration: Float

    abstract val valueFrom: T

    abstract val valueTo: T

    abstract val interpolator: Easing

    protected abstract fun interpolate(progress: Float): R

    fun animate(time: Float): R {

        val progress = if (time < startOffset) {
            0f
        } else {
            ((time - startOffset) / duration).coerceIn(0f, 1f)
        }

        return interpolate(interpolator.transform(progress))
    }
}