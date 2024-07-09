package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun unresolvedReference(ref : String, obj : String? = null) : Nothing =
    if (obj != null)
        error("Unresolved reference '$ref' for $obj")
    else error("Unresolved reference")


internal fun <T, R : Any> Expression.cast(block: (T) -> R) : Expression =
    Expression { property, context, state -> block(invoke(property, context, state) as T) }

internal fun <T, R : Any> Expression.withCast(block: T.(
    property: RawProperty<Any>,
    context: EvaluationContext,
    state: AnimationState
) -> R) : Expression = Expression { property, context, state ->
    block(invoke(property, context, state) as T, property, context, state)
}

internal operator fun Any.get(index : Int) : Any {
    return when (this){
        is Map<*,*> -> {
            (this as Map<Int, *>).get(index)
        }
        is List<*> -> this.get(index)
        is Array<*> -> this.get(index)
        is Vec2 -> when (index){
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds [0,1]")
        }
        is CharSequence -> this.get(index)
        is Color -> when(index){
            0 -> red
            1 -> green
            2 -> blue
            3 -> alpha
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds [0,3]")
        }
        else -> error("Can't get value by index from $this")
    }!!
}