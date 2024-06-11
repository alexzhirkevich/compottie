package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import kotlin.math.abs
import kotlin.math.absoluteValue

internal abstract class Keyframe<out T>() {
    abstract val start: T?
    abstract val end: T?
    abstract val time: Float
    abstract val hold : BooleanInt
    abstract val inValue: BezierInterpolation?
    abstract val outValue: BezierInterpolation?

    val endHold get() = if (hold == BooleanInt.Yes) start else end

    val easingX: Easing by lazy {
        val i = inValue
        val o = outValue

        if (hold == BooleanInt.Yes || i == null || o == null) {
            LinearEasing
        } else {
            if (i.x.isNotEmpty() &&
                i.y.isNotEmpty() &&
                o.x.isNotEmpty() &&
                o.y.isNotEmpty()
            ) {
                PreciseCubicBezier(
                    o.x[0].clampX(),
                    o.y[0].clampY(),
                    i.x[0].clampX(),
                    i.y[0].clampY()
                )
            } else LinearEasing
        }
    }

    val easingY by lazy {

        val i = inValue
        val o = outValue

        if (hold == BooleanInt.Yes || i == null || o == null) {
            return@lazy LinearEasing
        }

        if (i.x.size < 2 || i.y.size < 2 || o.x.size < 2 || o.y.size == 2) {
            return@lazy easingX
        }

        PreciseCubicBezier(
            o.x[1].clampX(),
            o.y[1].clampY(),
            i.x[1].clampX(),
            i.y[1].clampY()
        )
    }
}

private fun Float.clampX() =  coerceIn(-1f,1f)
private fun Float.clampY() =  coerceIn(-100f,100f)

private class PreciseCubicBezier(
    private val a: Float,
    private val b: Float,
    private val c: Float,
    private val d: Float
) : Easing {

    private val isLinear = (abs(a - b) < CubicErrorBound && abs(c-d) < CubicErrorBound)

    private fun evaluateCubic(a: Float, b: Float, m: Float): Float {
        return 3 * a * (1 - m) * (1 - m) * m +
                3 * b * (1 - m) * /*    */ m * m +
                /*                      */ m * m * m
    }

    override fun transform(fraction: Float): Float {

        if (isLinear)
            return fraction

        if (fraction > 0f && fraction < 1f) {
            var start = 0.0f
            var end = 1.0f
            while (true) {
                val midpoint = (start + end) / 2
                val estimate = evaluateCubic(a, c, midpoint)
                if ((fraction - estimate).absoluteValue < CubicErrorBound)
                    return evaluateCubic(b, d, midpoint)
                if (estimate < fraction)
                    start = midpoint
                else
                    end = midpoint
            }
        } else {
            return fraction
        }
    }
}

private const val CubicErrorBound: Float = 0.0001f


