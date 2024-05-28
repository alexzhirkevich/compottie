package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.Serializable

@Serializable
class BezierCurveInterpolation(
    val x : FloatArray,
    val y : FloatArray
)

