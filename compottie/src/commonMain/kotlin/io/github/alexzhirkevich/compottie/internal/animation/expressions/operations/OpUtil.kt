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
    else error("Unresolved reference: $ref")


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
    return checkNotNull(tryGet(index)){
        "Index $index out of bounds of $this length"
    }
}

internal fun Any.tryGet(index : Int) : Any? {
    return when (this){
        is Map<*,*> -> {
            (this as Map<Int, *>).get(index)
        }
        is List<*> -> this.getOrNull(index)
        is Array<*> -> this.getOrNull(index)
        is Vec2 -> when (index){
            0 -> x
            1 -> y
            else -> null
        }
        is CharSequence -> this.getOrNull(index)
        is Color -> when(index){
            0 -> red
            1 -> green
            2 -> blue
            3 -> alpha
            else -> null
        }
        else -> error("Can't get value by index from $this")
    }!!
}

