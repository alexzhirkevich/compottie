package io.github.alexzhirkevich.compottie.internal.utils

import kotlin.math.PI

object Math {
    fun toRadians(degree : Float) : Float {
        return  (degree * PI / 180).toFloat()
    }
}