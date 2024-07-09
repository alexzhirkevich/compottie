package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpDiv(
    private val a : Expression,
    private val b : Expression,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return invoke(
            a(property, context, state),
            b(property, context, state)
        )
    }

    companion object {
        operator fun invoke(a: Any, b: Any): Any {
            return when {
                (a is Number && b is Number) -> a.toFloat() / b.toFloat()
                (a is List<*> && b is Number) -> {
                    a as List<Number>
                    val bf = b.toFloat()
                    a.fastMap { it.toFloat() / bf }
                }

                else -> error("Cant divide $a by $b")
            }
        }
    }
}

internal operator fun Any.div(other : Any) : Any = OpDiv.invoke(this, other)