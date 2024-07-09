package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpPropertyValue(
    private val property: Expression,
    private val timeRemapping : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val prop = property(property, context, state) as RawProperty<*>

        return if (timeRemapping == null) {
            prop.raw(state).toExpressionType()
        } else {
            val time = timeRemapping.invoke(prop, context, state)

            require(time is Number) {
                "Internal error. Unable to cast $time to Number"
            }

            state.onTime(time.toFloat()) {
                prop.raw(it).toExpressionType()
            }
        }
    }
}

internal fun Any.toExpressionType() : Any {
    return when (this) {
        is Vec2 -> listOf(x, y)
        is Color -> listOf(red, green, blue, alpha)
        else -> this
    }
}