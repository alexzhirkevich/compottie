package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.skriptie.ecmascript.ScriptEngine

public fun interface Script<C : ScriptContext> {
    public operator fun invoke(engine: ScriptEngine<C>): Any
}

