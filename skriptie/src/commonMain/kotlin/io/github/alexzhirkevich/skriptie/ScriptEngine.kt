package io.github.alexzhirkevich.skriptie

public interface ScriptEngine<C : ScriptContext> : ScriptInterpreter<C> {

    public val context : C

    public fun reset() {
        context.reset()
    }
}

public fun <C : ScriptContext> ScriptEngine<C>.invoke(script: String) : Any? {
    return interpret(script).invoke(context)
}

public fun <C : ScriptContext> ScriptEngine(
    context: C,
    interpreter: ScriptInterpreter<C>
): ScriptEngine<C> = object : ScriptEngine<C>, ScriptInterpreter<C> by interpreter {
    override val context: C
        get() = context
}