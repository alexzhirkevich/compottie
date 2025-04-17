package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.Transient

internal abstract class ExpressionProperty<T : Any> : AnimatedProperty<T>, ExpressionHolder {

    abstract val expression: String?

    @Transient
    open val expressionEvaluator: ExpressionEvaluator by lazy {
        expression?.let(::ExpressionEvaluator) ?: RawExpressionEvaluator
    }

    override fun prepareExpressions() {
        expressionEvaluator
    }

    abstract fun mapEvaluated(e: Any): T

    override fun interpolated(state: AnimationState): T {

        if (!state.enableExpressions){
            return raw(state)
        }
        val evaluator = expressionEvaluator
        val evaluated = evaluator.run { evaluate(state) }

        return if (evaluated is AnimatedProperty<*>){
            evaluated.interpolated(state) as T
        } else {
            mapEvaluated(evaluated)
        }
    }
}