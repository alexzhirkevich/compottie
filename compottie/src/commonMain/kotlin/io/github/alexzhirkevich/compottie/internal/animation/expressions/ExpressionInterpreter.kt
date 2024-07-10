package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface ExpressionInterpreter {
    fun interpret() : Expression
}