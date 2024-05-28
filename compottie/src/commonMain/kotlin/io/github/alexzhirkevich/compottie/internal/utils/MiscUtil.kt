package io.github.alexzhirkevich.compottie.internal.utils

fun floorMod(x: Float, y: Float): Int {
    return floorMod(x.toInt(), y.toInt())
}

private fun floorMod(x: Int, y: Int): Int {
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