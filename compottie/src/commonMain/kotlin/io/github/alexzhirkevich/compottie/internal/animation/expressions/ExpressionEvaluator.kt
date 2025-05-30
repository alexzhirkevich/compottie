package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.keight.Script
import io.github.alexzhirkevich.keight.invokeSync

internal interface ExpressionEvaluator : ExpressionHolder {
    fun evaluate(state: AnimationState): Any
}

internal fun ExpressionEvaluator(
    expression: String,
    property: RawProperty<*>
) : ExpressionEvaluator = ExpressionEvaluatorImpl(expression, property)


private class ExpressionEvaluatorImpl(
    private val expr: String,
    private val property: RawProperty<*>
) : ExpressionEvaluator {

    private var script: Script? = null

    override fun prepareExpressions(state: AnimationState) {
        script = state.scriptEngine.compile(expr)
    }

    override fun evaluate(state: AnimationState): Any {
        val script = script ?: return property.raw(state)

        return state.onProperty(property) {
            try {
                script
                    .invokeSync(it.scriptEngine.runtime)
                    ?.toKotlin(it.scriptEngine.runtime)
            } catch (t: Throwable) {
                null
            } ?: property.raw(it)
        }
    }
}
