package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.effects.EffectValue
import io.github.alexzhirkevich.keight.js.JsAny
import kotlinx.serialization.Transient

internal abstract class ExpressionProperty<T : Any> : AnimatedProperty<T>, ExpressionHolder {

    abstract val expression: String?

    override val jsCache: MutableMap<String, JsAny?> = HashMap()

    override var group: PropertyGroup? = null

    @Transient
    open val expressionEvaluator: ExpressionEvaluator? by lazy {
        expression?.let { ExpressionEvaluator(it, this) }
    }

    override fun prepareExpressions(state: AnimationState) {
        expressionEvaluator?.prepareExpressions(state)
    }

    abstract fun mapEvaluated(e: Any): T

    override fun interpolated(state: AnimationState): T {

        if (!state.enableExpressions) {
            return raw(state)
        }

        val evaluated = expressionEvaluator?.evaluate(state) ?: return raw(state)

        return when (evaluated) {
            is AnimatedProperty<*> -> evaluated.interpolated(state) as T
            is RawProperty<*> -> evaluated.raw(state) as T
            is EffectValue<*> -> (evaluated.value?.raw(state) as T?) ?: raw(state)
            else -> try {
                mapEvaluated(evaluated)
            } catch (t: Throwable) {
                raw(state)
            }
        }
    }
}