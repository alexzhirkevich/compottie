package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.InterpretationContext

public open class ESInterpretationContext(
    public val namedArgumentsEnabled : Boolean = false
) : InterpretationContext {
    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
        return null
    }
}