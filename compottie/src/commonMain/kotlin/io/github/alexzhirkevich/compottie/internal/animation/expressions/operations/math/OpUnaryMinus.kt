package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpUnaryMinus(
    private val v : Expression,
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {

        return when (val v = v(property, context, state)) {
            is Number -> -v.toFloat()
            is List<*> -> {
                v as List<Number>
                v.fastMap { -it.toFloat() }
            }

            else -> error("Cant apply unary minus to $v")
        }
    }
}