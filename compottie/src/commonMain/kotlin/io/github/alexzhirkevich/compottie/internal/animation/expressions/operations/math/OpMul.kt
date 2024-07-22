package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpMul(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) * b(property, context, state)
}

internal fun OpMul(a : Any, b : Any) : Any = a.times(b)


internal operator fun Any.times(other : Any) : Any  {
    val a = this

    return when {
        a is Number && other is Number -> a.toFloat() * other.toFloat()
        a is List<*> && other is Number -> {
            a as List<Number>
            val bf = other.toFloat()
            a.fastMap { it.toFloat() * bf }
        }
        a is Number && other is List<*> -> {
            other as List<Number>
            val af = a.toFloat()
            other.fastMap { it.toFloat() * af }
        }
        a is CharSequence || other is CharSequence -> {
            a.toString().toFloat() * other.toString().toFloat()
        }
        else -> error("Cant multiply $a by $other")
    }
}