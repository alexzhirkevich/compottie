package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime

public interface ESAny {

    public val type : String get() = "object"

    public operator fun get(variable: String): Any?

    public operator fun invoke(function: String, context: ScriptRuntime, arguments: List<Expression>): Any? {
        return when {
            function == "toString" && arguments.isEmpty() -> toString()
            else -> null
        }
    }
}