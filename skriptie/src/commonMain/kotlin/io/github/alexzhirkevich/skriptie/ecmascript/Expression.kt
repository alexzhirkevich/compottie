package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpIndex
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.OpGetVariable

public interface Expression<in C : ScriptContext> {

    public operator fun invoke(context: C) : Any
}

internal fun <C : ScriptContext> Expression(
    block : (C) -> Any
) : Expression<C> = object  : Expression<C> {

    override fun invoke(context: C): Any {
        return block(context)
    }
}

internal fun Expression<*>.isAssignable() : Boolean {
    return this is OpGetVariable && assignmentType == null ||
            this is OpIndex && variable is OpGetVariable
}