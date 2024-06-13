package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.layout.ScaleFactor
import io.github.alexzhirkevich.compottie.dynamic.DynamicTransformProvider
import io.github.alexzhirkevich.compottie.dynamic.Identity
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.toScaleFactor
import io.github.alexzhirkevich.compottie.dynamic.toVec2
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.utils.Math
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.compottie.internal.utils.preRotate
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import io.github.alexzhirkevich.compottie.internal.utils.setValues
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal abstract class AnimatedTransform {

    abstract val anchorPoint: AnimatedVector2?
    abstract val position: AnimatedVector2?
    abstract val scale: AnimatedVector2?
    abstract val rotation: AnimatedNumber?
    abstract val opacity: AnimatedNumber?
    abstract val skew: AnimatedNumber?
    abstract val skewAxis: AnimatedNumber?

    var autoOrient = false
    var dynamic : DynamicTransformProvider? = null

    protected val matrix: Matrix = Matrix()

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

    fun matrix(state: AnimationState): Matrix {

        matrix.reset()

        var interpolatedPosition = position?.interpolated(state)
            ?.takeIf { it.x != 0f || it.y != 0f }
            ?.also {
                matrix.preTranslate(it.x, it.y)
            }

        dynamic?.offset?.let {
            interpolatedPosition = it.derive(interpolatedPosition ?: Offset.Zero, state)
        }

        if (autoOrient){
            if (interpolatedPosition != null) {
                // Store the start X and Y values because the pointF will be overwritten by the next getValue call.
                val startX = interpolatedPosition!!.x
                val startY = interpolatedPosition!!.y
                // 1) Find the next position value.
                // 2) Create a vector from the current position to the next position.
                // 3) Find the angle of that vector to the X axis (0 degrees).
                val nextPosition = state.remapped(state.frame + 0.001f) {
                    position!!.interpolated(it)
                }

                var rotationValue= Math.toDegree(
                    atan2(
                        (nextPosition.y - startY),
                        (nextPosition.x - startX)
                    )
                )

                dynamic?.rotation?.let {
                    rotationValue = it.derive(rotationValue, state)
                }

                matrix.preRotate(rotationValue)
            }
        } else {
            var rotation = rotation?.interpolated(state)
                ?.takeIf { it != 0f }

            dynamic?.rotation?.let {
                rotation = it.derive(rotation ?: 0f, state)
            }

            rotation?.let(matrix::preRotate)
        }

        var skew = skew?.interpolated(state)
            ?.takeIf { it != 0f }

        dynamic?.skew?.let {
            skew = it.derive(skew ?: 0f, state)
        }

        skew?.let { sk ->
            var skewAngle = skewAxis?.interpolated(state)

            dynamic?.skewAxis?.let {
                skewAngle = it.derive(skewAngle ?: 0f, state)
            }

            val mCos = if (skewAngle == null)
                0f
            else cos(Math.toRadians(-skewAngle!! + 90))

            val mSin = if (skewAngle == null)
                1f
            else sin(Math.toRadians(-skewAngle!! + 90))

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

        var scale = scale?.interpolatedNorm(state)
            ?.takeIf { it.x != 1f || it.y != 1f }

        dynamic?.scale?.let {
            scale = it.derive(scale?.toScaleFactor() ?: ScaleFactor.Identity, state).toVec2()
        }

        scale?.let {
            matrix.preScale(it.x, it.y)
        }


        anchorPoint?.interpolated(state)
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
