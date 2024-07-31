package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.GlobalContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.ScriptInterpreter

public class EcmascriptInterpreter<C : ScriptContext>(
    private val context : GlobalContext<C>,
) : ScriptInterpreter<C> {
    override fun interpret(script: String): Script<C> {
        return EcmascriptInterpreterImpl(script, context).interpret()
    }
}
