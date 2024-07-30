package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.toExpressionType
import io.github.alexzhirkevich.compottie.internal.shapes.EllipseShape
import io.github.alexzhirkevich.compottie.internal.shapes.FillShape
import io.github.alexzhirkevich.compottie.internal.shapes.PathShape
import io.github.alexzhirkevich.compottie.internal.shapes.RectShape
import io.github.alexzhirkevich.compottie.internal.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.shapes.TransformShape

internal abstract class OpShapeContext : ExpressionContext<Shape> {
    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
        return if (args != null) {
            when (callable) {
                "content" -> {
                    checkArgs(args, 1, callable)
                    OpGetShape(
                        layerOrGroup = this,
                        name = args.argAt(0)
                    )
                }

                else -> null
            }
        } else {
            when (callable) {
                "size" -> OpShapeSize(this)
                "position" -> OpShapePosition(this)
                "color" -> OpShapeColor(this)
                "path" -> OpShapePath(this)
                "scale" -> OpTransformShapeProperty(this, TransformShape::scale, callable)
                "rotation" -> OpTransformShapeProperty(this, TransformShape::rotation, callable)
                "rotationX" -> OpTransformShapeProperty(this, TransformShape::rotationX, callable)
                "rotationY" -> OpTransformShapeProperty(this, TransformShape::rotationY, callable)
                "rotationZ" -> OpTransformShapeProperty(this, TransformShape::rotationZ, callable)
                "skew" -> OpTransformShapeProperty(this, TransformShape::skew, callable)
                "skewAxis" -> OpTransformShapeProperty(this, TransformShape::skewAxis, callable)
                "opacity" -> OpTransformShapeProperty(this, TransformShape::opacity, callable)
                else -> null
            }
        }
    }
}

private fun OpShapeColor(
    shape : Expression
) = Expression { property, context, state ->
    val shape = shape(property, context, state)

    if (shape is FillShape){
        shape.color.interpolated(state).toExpressionType()
    } else {
        unresolvedReference("color", shape::class.simpleName)
    }

}

private fun OpShapeSize(
    shape : Expression
) = Expression { property, context, state ->
    val size = when (val shape = shape(property, context, state)){
        is EllipseShape -> shape.size
        is RectShape -> shape.size
        else -> unresolvedReference("size", shape::class.simpleName)
    }
    size.interpolated(state).toExpressionType()
}

private fun OpShapePosition(
    shape : Expression
) = Expression { property, context, state ->
    val position = when (val shape = shape(property, context, state)){
        is EllipseShape -> shape.position
        is RectShape -> shape.position
        is TransformShape -> shape.position
        else -> unresolvedReference("position", shape::class.simpleName)
    }
    position.interpolated(state).toExpressionType()
}

private fun OpTransformShapeProperty(
    shape: Expression,
    property : (TransformShape) -> AnimatedProperty<*>?,
    name : String
) = Expression { property, context, state ->
    val value = when (val shape = shape(property, context, state)) {
        is TransformShape -> property(shape)
        else -> unresolvedReference(name, shape::class.simpleName)
    }
    value?.interpolated(state)?.toExpressionType() ?: Undefined
}


private fun OpShapePath(
    shape : Expression
) = Expression { property, context, state ->
    val shape = shape(property, context, state)
    if(shape is PathShape) {
        shape.shape.interpolated(state).toExpressionType()
    } else {
        unresolvedReference("path", shape::class.simpleName)
    }
}