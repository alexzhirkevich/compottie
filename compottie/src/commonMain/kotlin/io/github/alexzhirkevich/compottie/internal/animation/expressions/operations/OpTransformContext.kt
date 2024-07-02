package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationContext

internal sealed class OpTransformContext : Operation, OperationContext {

    override fun evaluate(op: String, args: List<Operation>): Operation {
        return when(op) {
            "rotation" -> transformOp { _, _, s -> rotation.interpolated(s) }
            "scale" -> transformOp { _, _, s -> scale.interpolated(s) }
            "opacity" -> transformOp { _, _, s -> opacity.interpolated(s) }
            "skew" -> transformOp { _, _, s -> skew.interpolated(s) }
            "skewAxis" -> transformOp { _, _, s -> skewAxis.interpolated(s) }
            "position" -> transformOp { _, _, s -> position.interpolated(s) }

            else -> error("Unknown transform property: $op")
        }
    }

    private fun transformOp(
        block: AnimatedTransform.(
            value: Any,
            variables: Map<String, Any>,
            state: AnimationState
        ) -> Any
    ) = Operation { value, variables, state ->
        block(invoke(value, variables, state) as AnimatedTransform, value, variables, state)
    }
}