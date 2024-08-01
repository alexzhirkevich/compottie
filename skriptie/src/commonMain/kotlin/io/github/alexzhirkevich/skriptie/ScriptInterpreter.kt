package io.github.alexzhirkevich.skriptie

public interface ScriptInterpreter<C : ScriptRuntime> {

    public fun interpret(script : String) : Script<C>
}