package io.github.alexzhirkevich.skriptie

public interface ScriptEngine<C : ScriptContext> {

    public val context : C

    public fun compile(script : String) : Script<C>

    public fun reset()
}

public fun <C : ScriptContext> ScriptEngine<C>.invoke(script: String) : Any? {
    return compile(script).invoke(this)
}
