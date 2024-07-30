package io.github.alexzhirkevich.skriptie

public fun interface Script<C : ScriptContext> {
    public operator fun invoke(engine: ScriptEngine<C>): Any?
}

public fun <C : ScriptContext> Expression<C>.asScript(): Script<C> = Script { invoke(it.context) }

