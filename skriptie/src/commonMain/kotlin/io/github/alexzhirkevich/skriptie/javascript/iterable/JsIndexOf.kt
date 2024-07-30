package io.github.alexzhirkevich.skriptie.javascript.iterable

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.common.checkNotEmpty
import io.github.alexzhirkevich.skriptie.common.unresolvedReference
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext

internal class JsIndexOf(
    private val value : Expression<JSScriptContext>,
    private val search : Expression<JSScriptContext>,
    private val last : Boolean
) : Expression<JSScriptContext> {

    override fun invoke(
        context: JSScriptContext,
    ): Any? {
        val value = checkNotEmpty(value(context))
        val search = checkNotEmpty(search(context))

        return when {
            value is String && (search is String || search is Char) -> {
                if (search is String) {
                    if (last)
                        value.lastIndexOf(search)
                    else value.indexOf(search)
                } else {
                    value.indexOf(search as Char)
                }
            }

            value is List<*> -> if(last)
                value.lastIndexOf(search)
            else value.indexOf(search)
            else -> unresolvedReference("indexOf")
        }
    }
}

