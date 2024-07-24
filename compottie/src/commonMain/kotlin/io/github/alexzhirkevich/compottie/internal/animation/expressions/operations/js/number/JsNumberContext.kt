package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgsNotNull
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.JsContext

internal object JsNumberContext : JsContext {

    override fun interpret(parent: Expression, op: String?, args: List<Expression>?): Expression? {
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