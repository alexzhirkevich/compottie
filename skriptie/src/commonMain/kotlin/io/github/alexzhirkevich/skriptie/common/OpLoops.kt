package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke


internal class OpForLoop<C : ScriptRuntime>(
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

    override fun invokeRaw(
        context: C
    ): Any {

        if (assignment?.type == VariableType.Local || assignment?.type == VariableType.Const) {
            context.withScope(
                extraVariables = mapOf(
                    Pair(
                        assignment.variableName,
                        Pair(
                            assignment.type,
                            assignment.assignableValue(context,)
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
                body(ctx)
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


internal fun <C : ScriptRuntime> OpDoWhileLoop(
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


internal fun <C : ScriptRuntime> OpWhileLoop(
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
