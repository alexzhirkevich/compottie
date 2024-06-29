package io.github.alexzhirkevich.compottie.internal.utils

import kotlin.math.PI

private val PiDiv180 = PI / 180

object Math {
    fun toRadians(degree : Float) : Float {
        return (degree * PiDiv180).toFloat()
    }

    fun toDegree(radians : Float) : Float {
        return  (radians / PiDiv180).toFloat()
    }
}