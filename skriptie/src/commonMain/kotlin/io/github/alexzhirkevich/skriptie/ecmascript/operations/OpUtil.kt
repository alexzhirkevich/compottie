package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext

internal fun unresolvedReference(ref : String, obj : String? = null) : Nothing =
    if (obj != null)
        error("Unresolved reference '$ref' for $obj")
    else error("Unresolved reference: $ref")


internal fun <C : ScriptContext, T, R : Any> Expression<C>.cast(block: (T) -> R) : Expression<C> =
    Expression { block(invoke(it) as T) }

internal fun <C : ScriptContext, T, R : Any> Expression<C>.withCast(block: T.(
    context: C,
) -> R) : Expression<C> = Expression {
    block(invoke(it) as T, it)
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
        is CharSequence -> this.getOrNull(index)
        else -> error("Can't get value by index from $this")
    }!!
}

