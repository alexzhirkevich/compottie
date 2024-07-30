package io.github.alexzhirkevich.skriptie.javascript.number

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ExtensionContext
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgsNotNull
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext

internal object JsNumberContext : ExtensionContext<JSScriptContext> {

    override fun interpret(
        parent: Expression<JSScriptContext>,
        op: String?,
        args: List<Expression<JSScriptContext>>?
    ): Expression<JSScriptContext>? {
        return when(op){
            "toFixed" -> {
                checkArgsNotNull(args, op)
                JsToFixed(parent, args.singleOrNull())
            }
            "toPrecision" -> {
                checkArgsNotNull(args, op)
                JsToPrecision(parent, args.singleOrNull())
            }

            else -> null
        }
    }
}