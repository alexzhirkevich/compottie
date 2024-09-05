package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.ScriptInterpreter

public class ESInterpreter(
    private val langContext: LangContext,
    private val interpretationContext : InterpretationContext = ESInterpretationContext(false),
) : ScriptInterpreter {

    override fun interpret(script: String): Script {
        return ESInterpreterImpl(
            expr = script,
            langContext = langContext,
            globalContext = interpretationContext
        ).interpret()
    }
}
