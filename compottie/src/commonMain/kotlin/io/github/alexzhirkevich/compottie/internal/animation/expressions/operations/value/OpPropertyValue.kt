package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpPropertyValue(
    private val timeRemapping : Expression? = null
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (timeRemapping == null) {
            property.raw(state)
        } else {
            val time = timeRemapping.invoke(property, context, state)

            require(time is Number) {
                "Internal error. Unable to cast $time to Number"
            }

            val frame = (time.toFloat() - state.composition.startTime) *
                    state.composition.frameRate

            state.onFrame(frame) {
                property.raw(it)
            }
        }
    }
}