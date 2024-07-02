package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.PrecompositionLayer

internal sealed class OpLayerContext : Operation, OperationContext {

    override fun evaluate(op: String, args: List<Operation>): Operation {

        return when (op) {
            "index" -> layerOp { _, _, _ -> index ?: Undefined }
            "inPoint" -> layerOp { _, _, s -> inPoint?.div(s.composition.frameRate) ?: Undefined }
            "outPoint" -> layerOp { _, _, s -> outPoint?.div(s.composition.frameRate) ?: Undefined }
            "startTime" -> layerOp { _, _, s ->
                startTime?.div(s.composition.frameRate) ?: Undefined
            }

            "source" -> layerOp { _, _, _ -> if (this is PrecompositionLayer) refId else Undefined }
            "hasParent" -> layerOp { _, _, _ -> parentLayer != null }
            "parent" -> layerOp { _, _, _ -> parentLayer ?: Undefined }
            "transform" -> OpGetLayerTransform(this)

            else -> error("Unknown layer property: $op")
        }
    }

    private fun layerOp(
        block: Layer.(
            value: Any,
            variables: Map<String, Any>,
            state: AnimationState
        ) -> Any
    ) = Operation { value, variables, state ->
        block(invoke(value, variables, state) as Layer, value, variables, state)
    }
}