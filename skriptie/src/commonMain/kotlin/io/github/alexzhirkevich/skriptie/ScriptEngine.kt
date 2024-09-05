package io.github.alexzhirkevich.skriptie

public interface ScriptEngine : ScriptInterpreter {

    public val runtime : ScriptRuntime

    public fun reset() {
        runtime.reset()
    }
}

public fun ScriptEngine.invoke(script: String) : Any? {
    return interpret(script).invoke(runtime)
}

public fun ScriptEngine(
    runtime: ScriptRuntime,
    interpreter: ScriptInterpreter
): ScriptEngine = object : ScriptEngine, ScriptInterpreter by interpreter {
    override val runtime: ScriptRuntime get() = runtime
}