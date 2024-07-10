package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.Transient

internal abstract class ExpressionProperty<T : Any> : AnimatedProperty<T> {

    abstract val expression: String?

    @Transient
    open val expressionEvaluator: ExpressionEvaluator by lazy {
        expression?.let(::ExpressionEvaluator) ?: RawExpressionEvaluator
    }

    fun prepare() {
        expressionEvaluator
    }

    abstract fun mapEvaluated(e: Any): T

    override fun interpolated(state: AnimationState): T {
        val evaluator = expressionEvaluator
        return mapEvaluated(evaluator.run { evaluate(state) })
    }
}