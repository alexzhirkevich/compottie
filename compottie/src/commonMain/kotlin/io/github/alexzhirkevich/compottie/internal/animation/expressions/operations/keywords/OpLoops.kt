package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssign

internal class OpForLoop(
    private val assignment : OpAssign?,
    private val increment: Expression?,
    private val comparison : Expression?,
    private val body: Expression
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        if (comparison == null) {
            loop(
                condition = true,
                property = property,
                context = context,
                state = state
            )
        } else {

        }

        return Undefined
    }

    private fun loop(
        condition: Boolean,
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ) {
        val block = { ctx: EvaluationContext ->
            while (condition) {
                body.invoke(property, ctx, state)
                increment?.invoke(property, ctx, state)
            }
        }

        if (assignment?.type == VariableType.Let || assignment?.type == VariableType.Const) {
            context.withScope(
                extraVariables = mapOf(
                    Pair(
                        assignment.variableName,
                        Pair(
                            assignment.type,
                            assignment.assignableValue(
                                property,
                                context,
                                state
                            )
                        )
                    )
                ),
                block = block
            )
        } else {
            assignment?.invoke(property, context, state)
            context.withScope(block = block)
        }
    }
}


internal fun OpDoWhileLoop(
    condition : Expression,
    body : OpBlock
) = Expression { property, context, state ->
    do {
        body.invoke(property, context, state)
    } while (!condition.invoke(property, context, state).isFalse())
}


internal fun OpWhileLoop(
    condition : Expression,
    body : Expression
) = Expression { property, context, state ->
    while (!condition.invoke(property, context, state).isFalse()){
        body.invoke(property, context, state)
    }
}

internal fun Any.isFalse() : Boolean {
    return this == false
            || this is Number && toFloat() == 0f
            || this is CharSequence && isEmpty()
            || this is Undefined
}

