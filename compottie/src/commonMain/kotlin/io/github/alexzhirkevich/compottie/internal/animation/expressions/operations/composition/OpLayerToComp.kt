package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.get
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.totalTransformMatrix
import io.github.alexzhirkevich.compottie.internal.utils.fastSetFrom

internal class OpLayerToComp(
    private val layer : Expression,
    private val point : Expression,
    private val time : Expression? = null,
    private val reverse : Boolean
) : Expression {

    private val conversionMatrix = Matrix()

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        val layer = layer(property, context, state) as Layer

        val layerMatrix = layer.totalTransformMatrix(state)
        val compMatrix = state.currentComposition.transformMatrix(state)

        val point = point(property, context, state)

        val offset = Offset(
            (point[0] as Number).toFloat(),
            (point[1] as Number).toFloat()
        )

        if (reverse){
            conversionMatrix.fastSetFrom(layerMatrix)
            compMatrix.invert()
            conversionMatrix.timesAssign(compMatrix)
        } else {
            conversionMatrix.fastSetFrom(compMatrix)
            layerMatrix.invert()
            conversionMatrix.timesAssign(layerMatrix)
        }

        val result = conversionMatrix.map(offset)

        return listOf(result.x, result.y)
    }
}