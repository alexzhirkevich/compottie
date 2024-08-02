package io.github.alexzhirkevich.skriptie

public interface ScriptInterpreter {

    public fun interpret(script : String) : Script
}