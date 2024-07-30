package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgsNotNull
import io.github.alexzhirkevich.skriptie.ecmascript.ExtensionContext
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