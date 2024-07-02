package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import kotlin.math.PI

internal object OpGetPI : Operation {
    private const val floatPi = PI.toFloat()

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return floatPi
    }
}