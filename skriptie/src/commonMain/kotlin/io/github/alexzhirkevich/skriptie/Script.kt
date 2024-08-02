package io.github.alexzhirkevich.skriptie

public fun interface Script {
    public operator fun invoke(context: ScriptRuntime): Any?
}

public fun Expression.asScript(): Script = Script { invoke(it) }

