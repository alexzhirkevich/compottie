package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.OpAssign


public interface InterpretationContext<C : ScriptContext> : Expression<C> {

    override fun invoke(context: C): Any? = this

    public fun interpret(callable: String?, args: List<Expression<C>>?): Expression<C>?
}

internal fun <C: ScriptContext> List<Expression<C>>.argForNameOrIndex(
    index : Int,
    vararg name : String,
) : Expression<C>? {

    forEach { op ->
        if (op is OpAssign && name.any { op.variableName == it }) {
            return op.assignableValue
        }
    }

    return argAtOrNull(index)
}

internal fun <C: ScriptContext> List<Expression<C>>.argAt(
    index : Int,
) : Expression<C> {

    return get(index).let {
        if (it is OpAssign)
            it.assignableValue
        else it
    }
}

internal fun <C: ScriptContext> List<Expression<C>>.argAtOrNull(
    index : Int,
) : Expression<C>? {

    return getOrNull(index).let {
        if (it is OpAssign)
            it.assignableValue
        else it
    }
}