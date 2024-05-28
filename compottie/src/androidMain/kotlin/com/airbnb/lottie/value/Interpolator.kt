package com.airbnb.lottie.value

fun interface Interpolator {
    fun getInterpolation(var1: Float): Float

    companion object {
        val Linear = Interpolator { it }
    }
}
