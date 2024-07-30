package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface ExpressionInterpreter<C : ScriptContext> {
    fun interpret() : Expression<C>
}