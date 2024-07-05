package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext

internal sealed class OpTransformContext : Expression, ExpressionContext<AnimatedTransform> {

    override fun interpret(op: String, args: List<Expression>): Expression {
        return when(op) {
            "rotation" -> withContext { _, _, s -> rotation.interpolated(s) }
            "scale" -> withContext { _, _, s -> scale.interpolated(s) }
            "opacity" -> withContext { _, _, s -> opacity.interpolated(s) }
            "skew" -> withContext { _, _, s -> skew.interpolated(s) }
            "skewAxis" -> withContext { _, _, s -> skewAxis.interpolated(s) }
            "position" -> withContext { _, _, s -> position.interpolated(s) }

            else -> unresolvedProperty(op, "Transform")
        }
    }
}