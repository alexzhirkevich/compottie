package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext

internal sealed class OpCompositionContext : ExpressionContext<ExpressionComposition> {

    final override fun parse(
        op: String,
        args : List<Expression>
    ): Expression {
        return when (op) {
            "numLayers" -> withContext { _, _, _ -> layersCount }
            "width" -> withContext { _, _, _ -> width }
            "height" -> withContext { _, _, _ -> height }
            "displayStartTime" -> withContext { _, _, _ -> startTime }
            "frameDuration" -> withContext { _, _, _ -> durationFrames }
            "layer" -> OpGetLayer(
                comp = withContext { _, _, _ -> this },
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
}