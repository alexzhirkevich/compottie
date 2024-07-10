package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpFramesToTime(
    private val frame : Expression? = null,
    private val fps : Expression? = null,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val time = (frame?.invoke(property, context, state) as? Number)?.toFloat()
            ?: state.frame
        val fps = (fps?.invoke(property, context, state) as? Number)?.toFloat()
            ?: (1f / state.composition.frameRate)

        return  time / fps
    }
}