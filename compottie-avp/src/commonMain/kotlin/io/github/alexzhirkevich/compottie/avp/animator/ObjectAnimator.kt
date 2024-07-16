package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import io.github.alexzhirkevich.compottie.avp.xml.AnimatedVectorProperty

public sealed class ObjectAnimator<T, R> {

    public abstract val property : AnimatedVectorProperty<out ObjectAnimator<T,R>>

    public abstract val delay: Float

    public abstract val duration: Float

    public abstract val valueFrom: T

    public abstract val valueTo: T

    public abstract val easing: Easing

    public abstract val repeatCount : Int

    public abstract val repeatMode : RepeatMode

    protected abstract fun interpolate(progress: Float): R

    internal fun animate(time: Float): R {

        val progress = if (time < delay) {
            0f
        } else {
            val cycle = ((time - delay) / duration)

            if (cycle > repeatCount) {
                1f
            } else {
                (cycle - cycle.toInt()).coerceIn(0f, 1f).let {
                    if (repeatMode == RepeatMode.Reverse && cycle.toInt() % 2 == 0){
                        1f - it
                    } else it
                }
            }
        }

        return interpolate(easing.transform(progress))
    }
}

internal val ObjectAnimator<*,*>.endTime : Float
    get() = delay + duration * repeatCount