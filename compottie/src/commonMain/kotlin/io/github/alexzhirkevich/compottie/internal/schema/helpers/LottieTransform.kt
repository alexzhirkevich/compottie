package io.github.alexzhirkevich.compottie.internal.schema.helpers

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value

internal abstract class LottieTransform {

    abstract val anchorPoint : Vector
    abstract val position : Vector?
    abstract val scale : Vector?
    abstract val rotation : Value?
    abstract val opacity : Value
    abstract val skew: Value?
    abstract val skewAxis: Value?

    private val matrix : Matrix = Matrix()

    private var lastFrame : Int = -1

    fun matrix(time : Int) : Matrix {
        if (lastFrame == time) {
            return matrix
        }
        lastFrame = time

        matrix.reset()

        val (anchorX, anchorY) = anchorPoint.interpolated(time)

        val position = position?.interpolated(time)

        if (position != null && (position[0] != 0f || position[1] != 0f)) {
            matrix.translate(
                position[0] - anchorX,
                position[1] - anchorY,
            )
        }

        if (scale != null || rotation != null) {
            val (scaleX, scaleY) = scale?.interpolated(time) ?: defaultScale
            val rotation= rotation?.interpolated(time) ?: 0f

            matrix.translate(anchorX, anchorY)

            matrix.scale(
                x = scaleX / 100f,
                y = scaleY / 100f,
            )

            matrix.rotateZ(rotation)

            matrix.translate(-anchorX, -anchorY)
        }
        return matrix
    }
}

private val defaultScale = floatArrayOf(100f,100f)