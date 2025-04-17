package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

internal interface ExpressionEvaluator {
    fun RawProperty<*>.evaluate(state: AnimationState): Any
}

internal fun ExpressionEvaluator(expression: String, catchErrors : Boolean = true) : ExpressionEvaluator =
    ExpressionEvaluatorImpl(expression)

internal object RawExpressionEvaluator : ExpressionEvaluator {
    override fun RawProperty<*>.evaluate(state: AnimationState): Any = raw(state)
}


private class ExpressionEvaluatorImpl(
    expr: String
) : ExpressionEvaluator {

    private val context = DefaultEvaluatorContext()

    private val expression = kotlin.runCatching {
        ExpressionInterpreterImpl(expr, context).interpret()
    }.getOrNull()


    override fun RawProperty<*>.evaluate(state: AnimationState): Any {
        if (expression == null)
            return raw(state)

        return try {
            expression.invoke(this, context, state)
            context.result?.toListOrThis()
        } catch (t: Throwable) {
            null
        } finally {
            context.reset()
        } ?: raw(state)
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

