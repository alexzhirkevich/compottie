package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime

public interface ESAny<C : ScriptRuntime> {

    public val type : String get() = "object"

    public operator fun get(property: String): Any?

    public operator fun invoke(function: String, context: C, arguments: List<Expression<C>>): Any? {
        return when {
            function == "toString" && arguments.isEmpty() -> toString()
            else -> null
        }
    }
}