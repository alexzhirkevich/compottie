package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.shapes.GroupShape
import io.github.alexzhirkevich.compottie.internal.shapes.Shape

internal class OpGetShape(
    private val layerOrGroup : Expression,
    private val name : Expression
) : OpShapeContext() {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Shape {
        val layerOrGroup = layerOrGroup(property, context, state)
        val name = (name(property, context, state) as CharSequence).toString()

        val shape = when (layerOrGroup){
            is ShapeLayer -> {
                layerOrGroup.shapesByName[name]
            }
            is GroupShape -> {
                layerOrGroup.shapesByName[name]
            }
            else -> error("Can't get '$name' content of $layerOrGroup")
        }

        return checkNotNull(shape){
            "Content '$name' wasn't found"
        }
    }
}