package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.EcmascriptInterpretationContext
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.invoke

public open class JSGlobalContext(
    namedArgumentsEnabled : Boolean = false
) : EcmascriptInterpretationContext<JSRuntime>(namedArgumentsEnabled) {

    override fun interpret(
        callable: String?,
        args: List<Expression<JSRuntime>>?
    ): Expression<JSRuntime>? {
        return if (args != null){
            when (callable) {
                "typeof" -> {
                    checkArgs(args, 1, callable)
                    JsTypeof(args.argAt(0))
                }
                else -> null
            }
        } else null
    }
}



private fun JsTypeof(value : Expression<JSRuntime>) = Expression<JSRuntime> {
    value(it).let {
        when (it) {
            null -> "object"
            Unit -> "undefined"
            is ESAny<*> -> it.type
            else -> it::class.simpleName
        }
    }
}