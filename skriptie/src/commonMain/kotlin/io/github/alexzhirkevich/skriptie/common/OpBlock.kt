package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke


internal class OpBlock<C : ScriptRuntime>(
    val expressions: List<Expression<C>>,
    private val scoped : Boolean,
) : Expression<C> {

    override fun invokeRaw(context: C): Any? {
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
        val res = expression.invoke(context)
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

internal class OpReturn<C : ScriptRuntime>(
    val value : Expression<C>
) : Expression<C> by value

internal class OpContinue<C : ScriptRuntime> : Expression<C> by OpConstant(Unit)
internal class OpBreak<C : ScriptRuntime> : Expression<C> by OpConstant(Unit)

