package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke


internal class OpBlock(
    val expressions: List<Expression>,
    private val scoped : Boolean,
) : Expression {

    override fun invokeRaw(context: ScriptRuntime): Any? {
        return if (scoped) {
            context.withScope(block = ::invokeInternal)
        } else {
            invokeInternal(context)
        }
    }

    private fun invokeInternal(context: ScriptRuntime): Any? {
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

    private fun invoke(expression: Expression, context: ScriptRuntime) : Any? {
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

internal class OpReturn(
    val value : Expression
) : Expression by value

internal class OpContinue : Expression by OpConstant(Unit)
internal class OpBreak : Expression by OpConstant(Unit)

