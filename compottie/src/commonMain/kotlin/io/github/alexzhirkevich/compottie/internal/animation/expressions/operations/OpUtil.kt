package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

internal fun unresolvedProperty(property : String, obj : String) : Nothing =
    error("Unresolved property '$property' for $obj")