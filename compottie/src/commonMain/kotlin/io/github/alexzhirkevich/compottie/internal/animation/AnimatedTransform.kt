package io.github.alexzhirkevich.compottie.internal.schema.animation

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.utils.Math
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.preRotate
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import io.github.alexzhirkevich.compottie.internal.utils.setValues
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal abstract class AnimatedTransform {

    abstract val anchorPoint: AnimatedVector2?
    abstract val position: AnimatedVector2?
    abstract val scale: AnimatedVector2?
    abstract val rotation: AnimatedValue?
    abstract val opacity: AnimatedValue?
    abstract val skew: AnimatedValue?
    abstract val skewAxis: AnimatedValue?

    private val matrix: Matrix = Matrix()

    private val skewMatrix1: Matrix by lazy {
        Matrix()
    }

    private val skewMatrix2: Matrix by lazy {
        Matrix()
    }

    private val skewMatrix3: Matrix by lazy {
        Matrix()
    }

    private val skewValues: FloatArray by lazy {
        FloatArray(9)
    }

    fun matrix(frame: Float): Matrix {
        matrix.reset()

        position?.interpolated(frame)
            ?.takeIf { it.x != 0f || it.y != 0f }
            ?.let {
                matrix.preTranslate(it.x, it.y)
            }

        rotation?.interpolated(frame)
            ?.takeIf { it != 0f }
            ?.let(matrix::preRotate)

        skew?.interpolated(frame)
            ?.takeIf { it != 0f }
            ?.let { sk ->
                val skewAngle = skewAxis?.interpolated(frame)

                val mCos = if (skewAngle == null)
                    0f
                else cos(Math.toRadians(-skewAngle + 90))

                val mSin = if (skewAngle == null)
                    1f
                else sin(Math.toRadians(-skewAngle + 90))

                val aTan = tan(Math.toRadians(sk))

                clearSkewValues()
                skewValues[0] = mCos
                skewValues[1] = mSin
                skewValues[3] = -mSin
                skewValues[4] = mCos
                skewValues[8] = 1f
                skewMatrix1.setValues(skewValues)
                clearSkewValues()
                skewValues[0] = 1f
                skewValues[3] = aTan
                skewValues[4] = 1f
                skewValues[8] = 1f
                skewMatrix2.setValues(skewValues)
                clearSkewValues()
                skewValues[0] = mCos
                skewValues[1] = -mSin
                skewValues[3] = mSin
                skewValues[4] = mCos
                skewValues[8] = 1f

                skewMatrix3.setValues(skewValues)
                skewMatrix2.preConcat(skewMatrix1)
                skewMatrix3.preConcat(skewMatrix2)
                matrix.preConcat(skewMatrix3)
            }

        scale?.interpolated(frame)
            ?.takeIf { it.x != 1f || it.y != 1f }
            ?.let {
                matrix.preScale(it.x / 100f, it.y / 100f)
            }


        anchorPoint?.interpolated(frame)
            ?.takeIf { it.x != 0f || it.y != 0f }
            ?.let {
                matrix.preTranslate(-it.x, -it.y)
            }

        return matrix
    }

    private fun clearSkewValues() {
        for (i in 0..8) {
            skewValues[i] = 0f
        }
    }
}
