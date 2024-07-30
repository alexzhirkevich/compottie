package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedProperty
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.toExpressionType

internal sealed class OpTransformContext : Expression, ExpressionContext<AnimatedTransform> {

    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
        if (args != null)
            return null
        return when(callable) {
            "rotation" -> interpolate(AnimatedTransform::rotation)
            "rotationX" -> interpolate(AnimatedTransform::rotationX)
            "rotationY" -> interpolate(AnimatedTransform::rotationY)
            "rotationZ" -> interpolate(AnimatedTransform::rotationZ)
            "scale" ->  interpolate(AnimatedTransform::scale)
            "opacity" -> interpolate(AnimatedTransform::opacity)
            "skew" -> interpolate(AnimatedTransform::skew)
            "skewAxis" -> interpolate(AnimatedTransform::skewAxis)
            "position" -> interpolate(AnimatedTransform::position)
            else -> null
        }
    }

    private fun interpolate(value : (AnimatedTransform) -> AnimatedProperty<*>?) : Expression =
        withContext { _, _, s -> value(this)?.interpolated(s)?.toExpressionType() ?: Undefined }
}