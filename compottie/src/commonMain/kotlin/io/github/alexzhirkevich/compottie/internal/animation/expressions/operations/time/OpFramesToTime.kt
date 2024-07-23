package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpFramesToTime(
    frame : Expression? = null,
    fps : Expression? = null,
) = Expression { property, context, state ->
    val time = (frame?.invoke(property, context, state) as? Number)?.toFloat()
        ?: state.frame
    val fps = (fps?.invoke(property, context, state) as? Number)?.toFloat()
        ?: (1f / state.composition.frameRate)

    time / fps
}

internal fun OpTimeToFrames(
    time : Expression? = null,
    fps : Expression? = null,
    isDuration : Expression? = null
) = Expression { property, context, state ->
    val time = (time?.invoke(property, context, state) as? Number)?.toFloat()
        ?: (state.time.inWholeMilliseconds / 100f + state.currentComposition.startTime)
    val fps = (fps?.invoke(property, context, state) as? Number)?.toFloat()
        ?: (1f / state.composition.frameRate)

    val isDuration = (isDuration?.invoke(property, context, state) as? Boolean) ?: false

    if (isDuration) {
        (state.absoluteTime.inWholeMilliseconds / 100f + time) * fps
    } else {
        time * fps
    }
}