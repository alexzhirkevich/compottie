package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpPropertyValue(
    private val timeRemapping : Expression? = null
) : Expression {
    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        return if (timeRemapping == null) {
            property.rawInterpolated(state)
        } else {
            val time = timeRemapping.invoke(property, variables, state)

            require(time is Number) {
                "Internal error. Unable to cast $time to Number"
            }

            val frame = (time.toFloat() - state.composition.startTime) *
                    state.composition.frameRate

            state.onFrame(frame) {
                property.rawInterpolated(it)
            }
        }
    }
}