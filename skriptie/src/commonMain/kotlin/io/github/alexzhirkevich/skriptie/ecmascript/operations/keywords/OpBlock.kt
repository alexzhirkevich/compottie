package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpBlock<C : ScriptContext>(
    val expressions: List<Expression<C>>,
    private val scoped : Boolean,
) : Expression<C> {

    override fun invoke(context: C): Any {
        return if (scoped) {
            context.withScope {
                invokeInternal(it as C)
            }
        } else {
            invokeInternal(context)
        }
    }

    private fun invokeInternal(context: C): Any {
        if (expressions.isEmpty()) {
            return Undefined
        }

        if (expressions.size > 1) {
            repeat(expressions.size - 1) {
                val expr = expressions[it]
                val res = expr(context)

                if (expr is OpReturn) {
                    return res
                }
            }
        }
        return expressions.last().invoke(context)
    }
}