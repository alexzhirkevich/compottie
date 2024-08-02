package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.InterpretationContext

public abstract class EcmascriptInterpretationContext(
    public val namedArgumentsEnabled : Boolean = false
) : InterpretationContext {

}