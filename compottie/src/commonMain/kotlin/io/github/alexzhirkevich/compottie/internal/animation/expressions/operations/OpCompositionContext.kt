package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationContext

internal sealed class OpCompositionContext : Operation, OperationContext {

    final override fun evaluate(
        op: String,
        args : List<Operation>
    ): Operation {
        return when (op) {
            "numLayers" -> compOp { _, _, _ -> animation.layers.size }
            "width" -> compOp { _, _, _ -> width }
            "height" -> compOp { _, _, _ -> height }
            "displayStartTime" -> compOp { _, _, _ -> startTime }
            "frameDuration" -> compOp { _, _, _ -> durationFrames }
            "layer" -> OpGetLayer(
                name = { v, vars, s ->
                    val n = args.singleOrNull()?.invoke(v, vars, s) as? String
                    checkNotNull(n) {
                        "composition.layer(..) must take exactly one string parameter"
                    }
                }
            )

            else -> error("Unknown composition property: $op")
        }
    }
    private fun compOp(
        block: LottieComposition.(
            value: Any,
            variables: Map<String, Any>,
            state: AnimationState
        ) -> Any
    ) = Operation { value, variables, state ->
        block(invoke(value, variables, state) as LottieComposition, value, variables, state)
    }
}