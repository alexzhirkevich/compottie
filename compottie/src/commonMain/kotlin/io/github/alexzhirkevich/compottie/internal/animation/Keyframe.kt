package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt

internal abstract class Keyframe<out T> {
    abstract val start: T?
    abstract val end: T?
    abstract val time: Float
    abstract val hold : BooleanInt
    abstract val inValue: BezierInterpolation?
    abstract val outValue: BezierInterpolation?

    val endHold get() = if (hold == BooleanInt.Yes) start else end

    val easingX: Easing by lazy {
        if (hold == BooleanInt.Yes) {
            LinearEasing
        } else {
            val i = inValue
            val o = outValue

            if (!i?.x.isNullOrEmpty() &&
                !i?.y.isNullOrEmpty() &&
                !o?.x.isNullOrEmpty() && !o?.y.isNullOrEmpty()
            ) {
                CubicBezierEasing(
                    o!!.x[0].normalize(),
                    o.y[0]  .normalize(),
                    i!!.x[0].normalize(),
                    i.y[0]  .normalize()
                )
            } else LinearEasing
        }
    }

    val easingY by lazy {
        if (hold == BooleanInt.Yes) {
            LinearEasing
        } else {
            val i = inValue
            val o = outValue

            if (i?.x?.size == 2 && i.y.size == 2 && o?.x?.size == 2 && o.y.size == 2) {
                CubicBezierEasing(
                    o.x[1].normalize(),
                    o.y[1].normalize(),
                    i.x[1].normalize(),
                    i.y[1].normalize()
                )
            } else {
                easingX
            }
        }
    }
}

private fun Float.normalize() = coerceIn(0f,1f)

