package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.OpIndex

public fun interface Expression<in C : ScriptRuntime> {
    public fun invokeRaw(context: C): Any?

}

public operator fun <C: ScriptRuntime> Expression<C>.invoke(context: C): Any? =
    context.fromKotlin(invokeRaw(context))


internal fun Expression<*>.isAssignable() : Boolean {
    return this is OpGetVariable && assignmentType == null ||
            this is OpIndex && variable is OpGetVariable
}