package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.ScriptInterpreter

public class EcmascriptInterpreter(
    private val interpretationContext : InterpretationContext,
    private val langContext: LangContext
) : ScriptInterpreter {
    override fun interpret(script: String): Script {
        return EcmascriptInterpreterImpl(script, langContext, interpretationContext).interpret()
    }
}
