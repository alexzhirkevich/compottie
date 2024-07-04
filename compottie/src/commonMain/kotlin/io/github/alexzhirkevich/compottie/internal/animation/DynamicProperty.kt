package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.Serializable

@Serializable
internal abstract class DynamicProperty<T : Any> : AnimatedProperty<T> {

    open val expressionEvaluator: ExpressionEvaluator<T> = RawExpressionEvaluator()

    abstract val dynamic: PropertyProvider<T>?

    final override fun interpolated(state: AnimationState): T {
        val evaluator = expressionEvaluator
        val v = evaluator.run { evaluate(state) }
        return dynamic.derive(v, state)
    }
}