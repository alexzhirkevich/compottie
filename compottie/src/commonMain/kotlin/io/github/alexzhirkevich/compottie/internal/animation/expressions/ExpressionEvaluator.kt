package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty

internal interface ExpressionEvaluator<T : Any> {
    fun RawProperty<T>.evaluate(state: AnimationState): T
}


internal fun <T : Any> ExpressionEvaluator(expression: String) : ExpressionEvaluator<T> =
    ExpressionEvaluatorImpl(expression)

internal class RawExpressionEvaluator<T : Any> : ExpressionEvaluator<T> {
    override fun RawProperty<T>.evaluate(state: AnimationState): T = raw(state)
}


private class ExpressionEvaluatorImpl<T : Any>(expr : String) : ExpressionEvaluator<T> {

    private val context = DefaultEvaluatorContext()

    private val expression: Expression = MainExpressionInterpreter(expr).interpret()

    private var warned : Boolean = false

    @Suppress("unchecked_cast")
    override fun RawProperty<T>.evaluate(state: AnimationState): T {
        return try {
            if (state.enableExpressions) {
                context.reset()
                expression.invoke(this, context, state)
                context.result
            } else {
                raw(state)
            } as T
        } catch (t: Throwable) {
            if (!warned){
                warned = true
                Compottie.logger?.warn(
                    "Error occurred in a Lottie expression. Try disable expressions for Painter using enableExpressions=false: ${t.message}"
                )
            }
            raw(state)
        }
    }
}

