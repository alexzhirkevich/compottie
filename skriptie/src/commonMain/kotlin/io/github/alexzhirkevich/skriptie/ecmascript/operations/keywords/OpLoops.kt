package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssign

internal class OpForLoop<C : ScriptContext>(
    private val assignment : OpAssign<C>?,
    private val increment: Expression<C>?,
    private val comparison : Expression<C>?,
    private val body: Expression<C>
) : Expression<C> {

    override fun invoke(
        context: C
    ): Any {

        if (comparison == null) {
            loop(
                condition = true,
                context = context,
            )
        } else {
            TODO("for loop")
        }

        return Undefined
    }

    private fun loop(
        condition: Boolean,
        context: C,
    ) {
        val block = { ctx: C ->
            while (condition) {
                body.invoke(ctx)
                increment?.invoke(ctx)
            }
        }

        if (assignment?.type == VariableType.Let || assignment?.type == VariableType.Const) {
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
    }
}


internal fun <C : ScriptContext> OpDoWhileLoop(
    condition : Expression<C>,
    body : OpBlock<C>
) = Expression<C> {
    do {
        body.invoke(it)
    } while (!condition.invoke(it).isFalse())
}


internal fun <C : ScriptContext> OpWhileLoop(
    condition : Expression<C>,
    body : Expression<C>
) = Expression<C> {
    while (!condition.invoke(it).isFalse()){
        body.invoke(it)
    }
}

internal fun Any.isFalse() : Boolean {
    return this == false
            || this is Number && toDouble() == 0.0
            || this is CharSequence && isEmpty()
            || this is Undefined
}

