package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.OpIndex

public interface Expression<in C : ScriptContext> {

    public operator fun invoke(context: C): Any?
}

internal fun <C : ScriptContext> Expression(
    block : (C) -> Any?
) : Expression<C> = object  : Expression<C> {

    override fun invoke(context: C): Any? {
        return block(context)
    }
}

internal fun Expression<*>.isAssignable() : Boolean {
    return this is OpGetVariable && assignmentType == null ||
            this is OpIndex && variable is OpGetVariable
}