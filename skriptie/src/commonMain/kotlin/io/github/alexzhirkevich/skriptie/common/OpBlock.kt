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
                invoke(expressions[it], context)
            }
        }

        return invoke(expressions.last(), context)
    }

    private fun invoke(expression: Expression<C>, context: C) : Any? {
        val res = expression(context)
        return when(expression){
            is OpReturn -> throw BlockReturn(res)
            is OpContinue -> throw BlockContinue
            is OpBreak -> throw BlockBreak
            else -> res
        }
    }
}


internal sealed class BlockException : Throwable()
internal data object BlockContinue : BlockException()
internal data object BlockBreak : BlockException()
internal class BlockReturn(val value: Any?) : BlockException()

internal class OpReturn<C : ScriptContext>(
    val value : Expression<C>
) : Expression<C> by value

internal class OpContinue<C : ScriptContext> : Expression<C> by OpConstant(Unit)
internal class OpBreak<C : ScriptContext> : Expression<C> by OpConstant(Unit)

