package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.invoke

internal class ESObjectAccessor : ESFunctionBase("Object") {

    private val fKeys = "keys".func("o"){
        (it.firstOrNull() as? ESObject)?.keys ?: emptyList<String>()
    }

    private val fEntries = "entries".func("o"){
        (it.firstOrNull() as? ESObject)?.entries ?: emptyList<String>()
    }

    init {
        this["keys"] = fKeys
        this["entries"] = fEntries
    }

    override val functions: List<Function> = listOf(
        fKeys, fEntries
    )

    override fun invoke(args: List<Expression>, context: ScriptRuntime): Any? {
        return args.single().invoke(context) as ESObject
    }
}