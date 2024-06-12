package io.github.alexzhirkevich.compottie.internal.utils

import kotlin.math.max
import kotlin.math.min

fun floorMod(x: Float, y: Float): Int {
    return floorMod(x.toInt(), y.toInt())
}

fun floorMod(x: Int, y: Int): Int {
    return x - y * floorDiv(x, y)
}

private fun floorDiv(x: Int, y: Int): Int {
    var r = x / y
    val sameSign = (x xor y) >= 0
    val mod = x % y
    if (!sameSign && mod != 0) {
        r--
    }
    return r
}

internal fun hslToRed(h: Float, s: Float, l: Float): Float = hslToRgbComponent(0, h, s, l)
internal fun hslToGreen(h: Float, s: Float, l: Float): Float = hslToRgbComponent(8, h, s, l)
internal fun hslToBlue(h: Float, s: Float, l: Float): Float = hslToRgbComponent(4, h, s, l)

private fun hslToRgbComponent(n: Int, h: Float, s: Float, l: Float): Float {
    val k = (n.toFloat() + h / 30f) % 12f
    val a = s * min(l, 1f - l)
    return l - a * max(-1f, minOf(k - 3, 9 - k, 1f))
}