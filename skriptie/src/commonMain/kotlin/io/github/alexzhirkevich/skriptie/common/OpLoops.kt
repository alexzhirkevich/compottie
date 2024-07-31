package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.VariableType


internal class OpForLoop<C : ScriptContext>(
    private val assignment : OpAssign<C>?,
    private val increment: Expression<C>?,
    private val comparison : Expression<C>?,
    private val isFalse : (Any?) -> Boolean,
    private val body: Expression<C>
) : Expression<C> {


    private val condition: (C) -> Boolean = if (comparison == null) {
        { true }
    } else {
        { !isFalse(comparison.invoke(it)) }
    }

    override fun invoke(
        context: C
    ): Any {

        if (assignment?.type == VariableType.Local || assignment?.type == VariableType.Const) {
            context.withScope(
                extraVariables = mapOf(
                    Pair(
                        assignment.variableName,
                        Pair(
                            assignment.type,
                            assignment.assignableValue.invoke(context,)
                        )
                    )
                ),
            ) { block(it as C) }
        } else {
            assignment?.invoke(context)
            context.withScope { block(it as C) }
        }
        return Unit
    }

    private fun block(ctx: C) {
        while (condition(ctx)) {
            try {
                body.invoke(ctx)
            } catch (_: BlockContinue) {
                continue
            } catch (_: BlockBreak) {
                break
            } finally {
                increment?.invoke(ctx)
            }
        }
    }
}


internal fun <C : ScriptContext> OpDoWhileLoop(
    condition : Expression<C>,
    body : OpBlock<C>,
    isFalse : (Any?) -> Boolean
) = Expression<C> {
    do {
        try {
            body.invoke(it)
        } catch (_: BlockContinue) {
            continue
        } catch (_: BlockBreak) {
            break
        }
    } while (!isFalse(condition.invoke(it)))
}


internal fun <C : ScriptContext> OpWhileLoop(
    condition : Expression<C>,
    body : Expression<C>,
    isFalse : (Any?) -> Boolean
) = Expression<C> {
    while (!isFalse(condition.invoke(it))) {
        try {
            body.invoke(it)
        } catch (_: BlockContinue) {
            continue
        } catch (_: BlockBreak) {
            break
        }
    }
}
