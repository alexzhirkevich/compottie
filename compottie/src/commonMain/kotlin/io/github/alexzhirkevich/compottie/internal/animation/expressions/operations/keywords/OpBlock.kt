package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpBlock(
    val expressions: List<Expression>,
    private val scoped : Boolean,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (scoped) {
            context.withScope {
                invokeInternal(property, it, state)
            }
        } else {
            invokeInternal(property, context, state)
        }
    }

    private fun invokeInternal(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        if (expressions.isEmpty()) {
            return Undefined
        }

        if (expressions.size > 1) {
            repeat(expressions.size - 1) {
                val expr = expressions[it]
                val res = expr(property, context, state)

                if (expr is OpReturn) {
                    return res
                }
            }
        }
        return expressions.last().invoke(property, context, state)
    }
}