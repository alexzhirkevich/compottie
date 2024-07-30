package io.github.alexzhirkevich.skriptie

internal interface ScriptInterpreter<C : ScriptContext> {
    fun interpret() : Script<C>
}