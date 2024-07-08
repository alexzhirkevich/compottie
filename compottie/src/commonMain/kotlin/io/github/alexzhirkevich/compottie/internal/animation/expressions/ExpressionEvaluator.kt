package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

internal interface ExpressionEvaluator {
    fun RawProperty<*>.evaluate(state: AnimationState): Any
}


internal fun ExpressionEvaluator(expression: String) : ExpressionEvaluator =
    ExpressionEvaluatorImpl(expression)

internal object RawExpressionEvaluator : ExpressionEvaluator {
    override fun RawProperty<*>.evaluate(state: AnimationState): Any = raw(state)
}


private class ExpressionEvaluatorImpl(expr : String) : ExpressionEvaluator {

    private val context = DefaultEvaluatorContext()

    private val expression: Expression = MainExpressionInterpreter(expr).interpret()

    private var warned : Boolean = false

    override fun RawProperty<*>.evaluate(state: AnimationState): Any {
        return try {
            if (state.enableExpressions) {
                context.reset()
                expression.invoke(this, context, state)
                context.result.toListOrThis()
            } else {
                raw(state)
            }
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

private fun Any.toListOrThis() : Any{
    return when (this){
        is Map<*,*> -> values.toList()
        is Vec2 -> listOf(x,y)
        is Color -> listOf(red,green,blue,alpha)
        is Array<*> -> toList()
        else -> this
    }
}

