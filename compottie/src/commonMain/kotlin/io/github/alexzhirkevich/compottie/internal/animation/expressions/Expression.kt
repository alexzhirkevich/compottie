package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty

internal val EXPR_DEBUG_PRINT_ENABLED = false

internal fun interface Expression {

    operator fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any

    object UndefinedExpression: Expression {
        override fun invoke(
            property: RawProperty<Any>,
            context: EvaluationContext,
            state: AnimationState,
        ) = Undefined
    }
}