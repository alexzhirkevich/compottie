package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpConstant(value: Any) =
    Expression { _, _, _ -> value }