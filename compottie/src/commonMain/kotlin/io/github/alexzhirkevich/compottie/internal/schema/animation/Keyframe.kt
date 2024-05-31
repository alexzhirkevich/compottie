package io.github.alexzhirkevich.compottie.internal.schema.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

internal abstract class Keyframe<out T> {
    abstract val start: T?
    abstract val end: T?
    abstract val time: Float
    abstract val inValue: BezierInterpolation?
    abstract val outValue: BezierInterpolation?

    val easing: Easing by lazy {
        val i = inValue
        val o = outValue

        if (o != null && i != null) {
            CubicBezierEasing(o.x[0], o.y[0], i.x[0], i.y[0])
        } else LinearEasing
    }
}