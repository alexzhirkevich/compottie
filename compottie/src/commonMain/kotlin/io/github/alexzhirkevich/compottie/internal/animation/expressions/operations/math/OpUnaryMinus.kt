package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpUnaryMinus(a : Expression) = Expression { property, context, state ->
    when (val v = a(property, context, state)) {
        is Int -> -v
        is Number -> -v.toFloat()
        is List<*> -> {
            v as List<Number>
            v.fastMap { -it.toFloat() }
        }

        else -> error("Cant apply unary minus to $v")
    }
}