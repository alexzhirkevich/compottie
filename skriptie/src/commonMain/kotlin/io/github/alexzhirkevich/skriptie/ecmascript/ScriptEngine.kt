package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Script
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext

public interface ScriptEngine<C : ScriptContext> {

    public val context : C

    public fun compile(script : String) : Script<C>

    public fun reset()
}

public fun <C : ScriptContext> ScriptEngine<C>.invoke(script: String) : Any {
    return compile(script).invoke(this)
}
