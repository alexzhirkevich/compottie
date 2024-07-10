package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDiv

internal class OpNormalize(
    private val a : Expression,
) : Expression by OpDiv(a, OpLength(a))