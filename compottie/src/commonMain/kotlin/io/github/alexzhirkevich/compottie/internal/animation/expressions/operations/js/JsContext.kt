package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number.JsNumberContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string.JsStringContext

internal interface JsContext {

    fun interpret(parent: Expression, op: String?, args: List<Expression>?): Expression?

    companion object : JsContext {

        override fun interpret(parent: Expression, op: String?, args: List<Expression>?): Expression? {
            return if (args != null){
                when (op) {
                    "toString" -> Expression { p, v, s -> parent(p, v, s).toString() }
                    "indexOf", "lastIndexOf" -> {
                        checkArgs(args, 1, op)
                        JsIndexOf(
                            value = parent,
                            search = args.argAt(0),
                            last = op == "lastIndexOf"
                        )
                    }

                    else -> JsNumberContext.interpret(parent, op, args)
                        ?: JsStringContext.interpret(parent, op, args)
                }
            } else {
                JsNumberContext.interpret(parent, op, args)
                 ?: JsStringContext.interpret(parent, op, args)
            }
        }
    }
}

