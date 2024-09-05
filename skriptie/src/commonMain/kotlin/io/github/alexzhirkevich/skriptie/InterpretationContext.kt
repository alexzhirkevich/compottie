package io.github.alexzhirkevich.skriptie

public object DummyInterpretationContext : InterpretationContext {
    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
        return null
    }
}

public interface InterpretationContext :  Expression {

    override fun invokeRaw(context: ScriptRuntime): Any? = this

    public fun interpret(callable: String?, args: List<Expression>?): Expression?
}

internal fun  List<Expression>.argForNameOrIndex(
    index : Int,
    vararg name : String,
) : Expression? {

    return argAtOrNull(index)
//    forEach { op ->
//        if (op is OpAssign && name.any { op.variableName == it }) {
//            return op.assignableValue
//        }
//    }
//
//    return argAtOrNull(index)
}

internal fun  List<Expression>.argAt(
    index : Int,
) : Expression {

    return get(index)
//        .let {
//        if (it is OpAssign)
//            it.assignableValue
//        else it
//    }
}

internal fun  List<Expression>.argAtOrNull(
    index : Int,
) : Expression? {

    return getOrNull(index)
//        /**/.let {
//        if (it is OpAssign)
//            it.assignableValue
//        else it
//    }
}