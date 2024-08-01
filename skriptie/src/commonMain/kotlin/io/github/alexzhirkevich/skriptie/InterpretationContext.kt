package io.github.alexzhirkevich.skriptie


public interface InterpretationContext<C : ScriptRuntime> :  Expression<C> {

    override fun invokeRaw(context: C): Any? = this

    public fun interpret(callable: String?, args: List<Expression<C>>?): Expression<C>?
}

internal fun <C: ScriptRuntime> List<Expression<C>>.argForNameOrIndex(
    index : Int,
    vararg name : String,
) : Expression<C>? {

    return argAtOrNull(index)
//    forEach { op ->
//        if (op is OpAssign && name.any { op.variableName == it }) {
//            return op.assignableValue
//        }
//    }
//
//    return argAtOrNull(index)
}

internal fun <C: ScriptRuntime> List<Expression<C>>.argAt(
    index : Int,
) : Expression<C> {

    return get(index)
//        .let {
//        if (it is OpAssign)
//            it.assignableValue
//        else it
//    }
}

internal fun <C: ScriptRuntime> List<Expression<C>>.argAtOrNull(
    index : Int,
) : Expression<C>? {

    return getOrNull(index)
//        /**/.let {
//        if (it is OpAssign)
//            it.assignableValue
//        else it
//    }
}