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


    val easingX: Easing by lazy {
        val i = inValue
        val o = outValue

        if (!i?.x.isNullOrEmpty() &&
            !i?.y.isNullOrEmpty() &&
            !o?.x.isNullOrEmpty() && !o?.y.isNullOrEmpty()) {
            CubicBezierEasing(o!!.x[0], o.y[0], i!!.x[0], i.y[0])
        } else LinearEasing
    }

    val easingY by lazy {

        val i = inValue
        val o = outValue

        if ( i?.x?.size == 2 && i.y.size == 2 && o?.x?.size == 2 && o.y.size == 2){
            CubicBezierEasing(o.x[1], o.y[1], i.x[1], i.y[1])
        } else {
            easingX
        }
    }
}

