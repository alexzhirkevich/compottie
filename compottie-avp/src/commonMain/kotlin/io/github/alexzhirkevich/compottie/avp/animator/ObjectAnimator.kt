package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import io.github.alexzhirkevich.compottie.avp.xml.AnimatedVectorProperty

public sealed class ObjectAnimator<T, R> {

    public abstract val property : AnimatedVectorProperty<out ObjectAnimator<T,R>>

    public abstract val delay: Float

    public abstract val duration: Float

    public abstract val valueFrom: T

    public abstract val valueTo: T

    public abstract val easing: Easing

    protected abstract fun interpolate(progress: Float): R

    internal fun animate(time: Float): R {

        val progress = if (time < delay) {
            0f
        } else {
            ((time - delay) / duration).coerceIn(0f, 1f)
        }

        return interpolate(easing.transform(progress))
    }
}