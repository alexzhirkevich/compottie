package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.ScriptRuntime

public abstract class EcmascriptInterpretationContext<C : ScriptRuntime>(
    public val namedArgumentsEnabled : Boolean = false
) : InterpretationContext<C> {

}