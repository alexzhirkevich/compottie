package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.dynamic.DynamicTransformProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
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

internal abstract class AnimatedTransform{

    abstract val anchorPoint: AnimatedVector2?
    abstract val position: AnimatedVector2?
    abstract val scale: AnimatedVector2?
    abstract val rotation: AnimatedNumber?
    abstract val opacity: AnimatedNumber?
    abstract val skew: AnimatedNumber?
    abstract val skewAxis: AnimatedNumber?

    var dynamic : DynamicTransformProvider? = null
        set(value) {
            if (field !== value) {
                field = value
                position?.dynamicOffset(value?.offset)
                scale?.dynamicScale(value?.scale)
                rotation?.dynamic(value?.rotation)
                opacity?.dynamic(value?.opacity)
                skew?.dynamic(value?.skew)
                skewAxis?.dynamic(value?.skewAxis)
            }
        }

    open fun isHidden(state: AnimationState) : Boolean = false

    protected val matrix: Matrix = Matrix()

    private val skewMatrix1: Matrix = Matrix()

    private val skewMatrix2: Matrix = Matrix()

    private val skewMatrix3: Matrix = Matrix()

    private val skewValues: FloatArray = FloatArray(9)

    fun matrix(state: AnimationState): Matrix {
        val autoOrient = state.layer.autoOrient == BooleanInt.Yes

        matrix.reset()

        if (isHidden(state)){
            return matrix
        }

        val interpolatedPosition = position?.interpolated(state)
            ?.takeIf { it.x != 0f || it.y != 0f }
            ?.also {
                matrix.preTranslate(it.x, it.y)
            }

        if (autoOrient){
            if (interpolatedPosition != null) {
                // Store the start X and Y values because the pointF will be overwritten by the next getValue call.
                val startX = interpolatedPosition.x
                val startY = interpolatedPosition.y
                // 1) Find the next position value.
                // 2) Create a vector from the current position to the next position.
                // 3) Find the angle of that vector to the X axis (0 degrees).
                val nextPosition = state.onFrame(state.frame + 0.001f) {
                    position!!.interpolated(it)
                }

                val rotationValue= Math.toDegree(
                    atan2(
                        (nextPosition.y - startY),
                        (nextPosition.x - startX)
                    )
                )


                matrix.preRotate(rotationValue)
            }
        } else {
            val rotation = rotation?.interpolated(state)
                ?.takeIf { it != 0f }

            rotation?.let(matrix::preRotate)
        }

        val skew = skew?.interpolated(state)
            ?.takeIf { it != 0f }

        skew?.let { sk ->
            val skewAngle = skewAxis?.interpolated(state)

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

        val scale = scale?.interpolatedNorm(state)
            ?.takeIf { it.x != 1f || it.y != 1f }

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
        skewValues.fill(0f)
    }
}
