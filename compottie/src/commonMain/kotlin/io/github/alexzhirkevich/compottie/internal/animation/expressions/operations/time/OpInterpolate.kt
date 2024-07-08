package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.lerp as vecLerp
import io.github.alexzhirkevich.compottie.dynamic.toOffset
import androidx.compose.ui.util.lerp as numLerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant

internal class OpInterpolate(
    private val t : Expression,
    private val tMin : Expression,
    private val tMax : Expression,
    private val value1 : Expression,
    private val value2 : Expression,
    private val easing : Easing
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val t = (t(property, context, state) as Number).toFloat()
        val tMin = (tMin.invoke(property, context, state) as Number).toFloat()
        val tMAx = (tMax.invoke(property, context, state) as Number).toFloat()
        val value1 = value1(property, context, state)
        val value2 = value2(property, context, state)

        return when {
            t <= tMin -> value1
            t >= tMAx -> value2
            else -> interpolate(
                value1 = value1,
                value2 = value2,
                fraction = fraction(tMin, tMAx, t)
            )
        }
    }

    private fun fraction(a: Float, b: Float, time: Float) =
        easing.transform((time - a) / (b - a))

    private fun interpolate(value1: Any, value2: Any, fraction: Float): Any {
        return when {
            value1 is Number && value2 is Number ->
                return numLerp(value1.toFloat(), value2.toFloat(), fraction)

            value1 is Vec2 && value2 is Vec2 ->
                vecLerp(value1.toOffset(), value2.toOffset(), fraction)

            else -> error("Cant interpolate between $value1 and $value2")
        }
    }

    companion object {
        fun interpret(easing: Easing, args: List<Expression>): OpInterpolate = when (args.size) {
            3 -> OpInterpolate(
                t = args[0],
                tMin = OpConstant(0f),
                tMax = OpConstant(1f),
                value1 = args[1],
                value2 = args[2],
                easing = easing
            )

            5 -> OpInterpolate(
                t = args[0],
                tMin = args[1],
                tMax = args[2],
                value1 = args[3],
                value2 = args[4],
                easing = easing
            )

            else -> error("interpolation function can take 3 or 5 arguments but got ${args.size}")
        }
    }
}