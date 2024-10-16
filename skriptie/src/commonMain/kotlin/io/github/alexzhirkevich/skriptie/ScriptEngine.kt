package io.github.alexzhirkevich.skriptie

public interface ScriptEngine : ScriptInterpreter {

    public val runtime : ScriptRuntime

    public fun reset() {
        runtime.reset()
    }
}

public fun ScriptEngine.evaluate(script: String) : Any? {
    return invoke(interpret(script)) 
}

public fun ScriptEngine.invoke(script: Script) : Any? {
    return script.invoke(runtime)
}

public fun ScriptEngine(
    runtime: ScriptRuntime,
    interpreter: ScriptInterpreter
): ScriptEngine = object : ScriptEngine, ScriptInterpreter by interpreter {
    override val runtime: ScriptRuntime get() = runtime
}