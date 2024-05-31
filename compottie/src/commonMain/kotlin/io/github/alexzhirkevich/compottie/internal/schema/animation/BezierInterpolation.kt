package io.github.alexzhirkevich.compottie.internal.schema.animation

import kotlinx.serialization.Serializable

@Serializable
class BezierInterpolation(
    val x : FloatArray,
    val y : FloatArray
)

