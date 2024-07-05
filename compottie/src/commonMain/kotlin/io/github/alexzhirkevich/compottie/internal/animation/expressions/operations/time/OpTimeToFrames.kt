package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpTimeToFrames(
    private val time : Expression? = null,
    private val fps : Expression? = null,
    private val isDuration : Expression? = null
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val time = (time?.invoke(property, context, state) as? Number)?.toFloat()
            ?: (state.time.inWholeMilliseconds / 100f + state.currentComposition.startTime)
        val fps = (fps?.invoke(property, context, state) as? Number)?.toFloat()
            ?: (1f / state.composition.frameRate)

        val isDuration = (isDuration?.invoke(property, context, state) as? Boolean) ?: false

        return if (isDuration) {
            (state.absoluteTime.inWholeMilliseconds / 100f + time) * fps
        } else {
            time * fps
        }
    }
}