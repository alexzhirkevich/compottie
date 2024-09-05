package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke

internal class ESObjectAccessor : ESFunctionBase("Object") {

    init {
        this.set("keys", "keys".func("o"){
            (it.firstOrNull() as? ESObject)?.keys ?: emptyList<String>()
        })

        this.set("entries", "entries".func("o"){
            (it.firstOrNull() as? ESObject)?.entries ?: emptyList<String>()
        })
    }

    override fun invoke(args: List<Expression>, context: ScriptRuntime): Any? {
        return args.single().invoke(context) as ESObject
    }
}