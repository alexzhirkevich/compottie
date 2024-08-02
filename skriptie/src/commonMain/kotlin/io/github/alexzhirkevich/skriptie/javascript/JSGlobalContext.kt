package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.EcmascriptInterpretationContext
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.invoke

public open class JSGlobalContext(
    namedArgumentsEnabled : Boolean = false
) : EcmascriptInterpretationContext(namedArgumentsEnabled) {

    override fun interpret(
        callable: String?,
        args: List<Expression>?
    ): Expression? {
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



private fun JsTypeof(value : Expression) = Expression {
    value(it).let {
        when (it) {
            null -> "object"
            Unit -> "undefined"
            is ESAny -> it.type
            else -> it::class.simpleName
        }
    }
}