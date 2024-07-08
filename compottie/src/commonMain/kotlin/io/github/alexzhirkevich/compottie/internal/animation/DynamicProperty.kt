package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.Serializable

@Serializable
internal abstract class DynamicProperty<T : Any> : AnimatedProperty<T> {

    abstract val expression : String?

    abstract val  expressionEvaluator: ExpressionEvaluator

    abstract val dynamic: PropertyProvider<T>?

    abstract fun mapEvaluated(e: Any): T

    final override fun interpolated(state: AnimationState): T {
        val evaluator = expressionEvaluator
        val v = mapEvaluated(evaluator.run { evaluate(state) })
        return dynamic.derive(v, state)
    }
}