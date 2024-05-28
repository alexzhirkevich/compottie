package com.airbnb.lottie.utils

import kotlin.math.pow

/**
 * Use this instead of [android.animation.ArgbEvaluator] because it interpolates through the gamma color
 * space which looks better to us humans.
 *
 *
 * Written by Romain Guy and Francois Blavoet.
 * https://androidstudygroup.slack.com/archives/animation/p1476461064000335
 */
object GammaEvaluator {
    // Opto-electronic conversion function for the sRGB color space
    // Takes a gamma-encoded sRGB value and converts it to a linear sRGB value
    private fun OECF_sRGB(linear: Float): Float {
        // IEC 61966-2-1:1999
        return if (linear <= 0.0031308f) linear * 12.92f else ((linear.pow((1.0f / 2.4f)) * 1.055f) - 0.055f).toFloat()
    }

    // Electro-optical conversion function for the sRGB color space
    // Takes a linear sRGB value and converts it to a gamma-encoded sRGB value
    private fun EOCF_sRGB(srgb: Float): Float {
        // IEC 61966-2-1:1999
        return if (srgb <= 0.04045f) srgb / 12.92f else ((srgb + 0.055f) / 1.055f).pow(2.4f) as Float
    }

    fun evaluate(fraction: Float, startInt: Int, endInt: Int): Int {
        // Fast return in case start and end is the same
        // or if fraction is at start/end or out of [0,1] bounds
        if (startInt == endInt) {
            return startInt
        } else if (fraction <= 0f) {
            return startInt
        } else if (fraction >= 1f) {
            return endInt
        }

        val startA = ((startInt shr 24) and 0xff) / 255.0f
        var startR = ((startInt shr 16) and 0xff) / 255.0f
        var startG = ((startInt shr 8) and 0xff) / 255.0f
        var startB = (startInt and 0xff) / 255.0f

        val endA = ((endInt shr 24) and 0xff) / 255.0f
        var endR = ((endInt shr 16) and 0xff) / 255.0f
        var endG = ((endInt shr 8) and 0xff) / 255.0f
        var endB = (endInt and 0xff) / 255.0f

        // convert from sRGB to linear
        startR = EOCF_sRGB(startR)
        startG = EOCF_sRGB(startG)
        startB = EOCF_sRGB(startB)

        endR = EOCF_sRGB(endR)
        endG = EOCF_sRGB(endG)
        endB = EOCF_sRGB(endB)

        // compute the interpolated color in linear space
        var a = startA + fraction * (endA - startA)
        var r = startR + fraction * (endR - startR)
        var g = startG + fraction * (endG - startG)
        var b = startB + fraction * (endB - startB)

        // convert back to sRGB in the [0..255] range
        a = a * 255.0f
        r = OECF_sRGB(r) * 255.0f
        g = OECF_sRGB(g) * 255.0f
        b = OECF_sRGB(b) * 255.0f

        return Math.round(a) shl 24 or (Math.round(r) shl 16) or (Math.round(g) shl 8) or Math.round(
            b
        )
    }
}
