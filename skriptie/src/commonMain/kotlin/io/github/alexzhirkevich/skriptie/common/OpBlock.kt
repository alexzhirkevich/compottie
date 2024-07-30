package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal class OpBlock<C : ScriptContext>(
    val expressions: List<Expression<C>>,
    private val scoped : Boolean,
) : Expression<C> {

    override fun invoke(context: C): Any? {
        return if (scoped) {
            context.withScope {
                invokeInternal(it as C)
            }
        } else {
            invokeInternal(context)
        }
    }

    private fun invokeInternal(context: C): Any? {
        if (expressions.isEmpty()) {
            return Unit
        }

        if (expressions.size > 1) {
            repeat(expressions.size - 1) {
                val expr = expressions[it]
                val res = expr(context)

                if (expr is OpReturn<*>) {
                    return res
                }
            }
        }
        return expressions.last().invoke(context)
    }
}