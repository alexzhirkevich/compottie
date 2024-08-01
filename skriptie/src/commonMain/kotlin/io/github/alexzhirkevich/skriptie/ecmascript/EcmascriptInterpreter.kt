package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.ScriptInterpreter

public class EcmascriptInterpreter<C : ScriptRuntime>(
    private val interpretationContext : InterpretationContext<C>,
    private val langContext: LangContext
) : ScriptInterpreter<C> {
    override fun interpret(script: String): Script<C> {
        return EcmascriptInterpreterImpl(script, langContext, interpretationContext).interpret()
    }
}
