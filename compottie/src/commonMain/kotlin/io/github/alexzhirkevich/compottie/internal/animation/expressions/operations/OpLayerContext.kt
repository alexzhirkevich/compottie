package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.PrecompositionLayer

internal sealed class OpLayerContext : Expression, ExpressionContext<Layer> {

    override fun parse(op: String, args: List<Expression>): Expression {

        return when (op) {
            "index" -> withContext { _, _, _ -> index ?: Undefined }
            "inPoint" -> withContext { _, _, s ->
                inPoint?.div(s.composition.frameRate) ?: Undefined
            }
            "outPoint" -> withContext { _, _, s ->
                outPoint?.div(s.composition.frameRate) ?: Undefined
            }
            "startTime" -> withContext { _, _, s ->
                startTime?.div(s.composition.frameRate) ?: Undefined
            }

            "source" -> withContext { _, _, _ ->
                if (this is PrecompositionLayer) refId else Undefined
            }
            "hasParent" -> withContext { _, _, _ -> parentLayer != null }
            "parent" -> withContext { _, _, _ -> parentLayer ?: Undefined }
            "transform" -> OpGetLayerTransform(this)
            "effect" -> {
                checkArgs(args, 1, op)
                OpGetEffect(layer = this, nameOrIndex = args[0])
            }

            "sourceRectAtTime" -> error("$op for Layer is not yet supported")
            else -> unresolvedProperty(op, "Layer")
        }
    }
}