package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpReturn(
    val value : Expression
) : Expression by value