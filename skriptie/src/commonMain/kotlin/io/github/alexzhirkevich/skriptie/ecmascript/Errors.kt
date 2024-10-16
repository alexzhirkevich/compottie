package io.github.alexzhirkevich.skriptie.ecmascript

public open class ESError(message : String?, cause : Throwable?) : Exception(message, cause), ESAny {
    override fun get(variable: Any?): Any? {
        return when(variable){
            "message" -> message
            "stack" -> stackTraceToString()
            "name" -> this::class.simpleName
            else -> Unit
        }
    }

    override fun contains(variable: Any?): Boolean {
        return variable == "message" || variable == "stack" || variable == "name"
    }
}

public class SyntaxError(message : String? = null, cause : Throwable? = null) : ESError(message, cause)

public class TypeError(message : String? = null, cause : Throwable? = null) : ESError(message, cause)

public class ReferenceError(message : String? = null, cause : Throwable? = null) : ESError(message, cause)

internal fun unresolvedReference(ref : String, obj : String? = null) : Nothing =
    if (obj != null)
        throw ReferenceError("Unresolved reference '$ref' for $obj")
    else throw ReferenceError("$ref is not defined")