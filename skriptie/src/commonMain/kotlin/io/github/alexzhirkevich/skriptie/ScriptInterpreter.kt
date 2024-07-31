package io.github.alexzhirkevich.skriptie

public interface ScriptInterpreter<C : ScriptContext> {

    public fun interpret(script : String) : Script<C>
}