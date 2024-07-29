package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface Script {
    operator fun invoke(): Any
}