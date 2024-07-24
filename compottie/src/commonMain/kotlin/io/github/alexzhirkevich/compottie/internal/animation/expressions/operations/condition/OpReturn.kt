package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.condition

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpReturn(
    val value : Expression
) : Expression by value