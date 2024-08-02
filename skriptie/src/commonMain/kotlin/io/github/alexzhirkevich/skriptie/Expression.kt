package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.OpIndex

public fun interface Expression {
    public fun invokeRaw(context: ScriptRuntime): Any?

}

public operator fun Expression.invoke(context: ScriptRuntime): Any? =
    context.fromKotlin(invokeRaw(context))


internal fun Expression.isAssignable() : Boolean {
    return this is OpGetVariable && assignmentType == null ||
            this is OpIndex && variable is OpGetVariable
}