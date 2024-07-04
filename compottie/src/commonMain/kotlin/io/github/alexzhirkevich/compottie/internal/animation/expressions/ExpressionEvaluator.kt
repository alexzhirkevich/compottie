package io.github.alexzhirkevich.compottie.internal.animation.expressions

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
            throw ExpressionException(
                "Error occurred in a Lottie expression. Try disable expressions for Painter using enableExpressions=false",
                t
            )
        }
    }
}

