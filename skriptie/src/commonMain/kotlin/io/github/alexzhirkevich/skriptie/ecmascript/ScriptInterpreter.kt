package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface ScriptInterpreter<C : ScriptContext> {
    fun interpret() : Script<C>
}