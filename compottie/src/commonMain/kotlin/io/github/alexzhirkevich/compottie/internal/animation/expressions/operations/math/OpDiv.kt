package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpDiv(a : Expression, b : Expression) = Expression { property, context, state ->
    a(property, context, state) / b(property, context, state)
}

internal fun OpDiv(a : Any, b : Any) : Any = a.div(b)

internal operator fun Any.div(other : Any) : Any {
    val a = this
    return when {
        a is Number && other is Number -> a.toFloat() / other.toFloat()
        a is List<*> && other is Number -> {
            a as List<Number>
            val bf = other.toFloat()
            a.fastMap { it.toFloat() / bf }
        }
        a is CharSequence || other is CharSequence -> {
            a.toString().toFloat() / other.toString().toFloat()
        }
        else -> error("Cant divide $a by $other")
    }
}