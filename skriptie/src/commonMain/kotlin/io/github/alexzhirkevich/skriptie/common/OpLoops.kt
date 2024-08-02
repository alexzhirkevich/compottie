package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke


internal class OpForLoop(
    private val assignment : OpAssign?,
    private val increment: Expression?,
    private val comparison : Expression?,
    private val isFalse : (Any?) -> Boolean,
    private val body: Expression
) : Expression {


    private val condition: (ScriptRuntime) -> Boolean = if (comparison == null) {
        { true }
    } else {
        { !isFalse(comparison.invoke(it)) }
    }

    override fun invokeRaw(
        context: ScriptRuntime
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
            ) { block(it) }
        } else {
            assignment?.invoke(context)
            context.withScope { block(it) }
        }
        return Unit
    }

    private fun block(ctx: ScriptRuntime) {
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


internal fun  OpDoWhileLoop(
    condition : Expression,
    body : OpBlock,
    isFalse : (Any?) -> Boolean
) = Expression {
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


internal fun  OpWhileLoop(
    condition : Expression,
    body : Expression,
    isFalse : (Any?) -> Boolean
) = Expression {
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
