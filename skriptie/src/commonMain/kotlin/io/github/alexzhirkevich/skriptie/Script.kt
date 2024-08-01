package io.github.alexzhirkevich.skriptie

public fun interface Script<C : ScriptRuntime> {
    public operator fun invoke(context: C): Any?
}

public fun <C : ScriptRuntime> Expression<C>.asScript(): Script<C> = Script { invoke(it) }

