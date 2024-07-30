package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.number.JsNumberContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string.JsStringContext
import io.github.alexzhirkevich.skriptie.ecmascript.ExtensionContext
import io.github.alexzhirkevich.skriptie.javascript.iterable.JsIndexOf


public open class JsExtensionContext: ExtensionContext<JSScriptContext> {

    override fun interpret(
        parent: Expression<JSScriptContext>,
        op: String?,
        args: List<Expression<JSScriptContext>>?
    ): Expression<JSScriptContext>? {
        return if (args != null){
            when (op) {
                "toString" -> Expression<JSScriptContext> { parent(it).toString() }
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

