package io.github.alexzhirkevich.skriptie.common

public sealed class SkriptieError(message : String?, cause : Throwable?) : Exception(message, cause)

public class SyntaxError(message : String? = null, cause : Throwable? = null) : SkriptieError(message, cause)

public class TypeError(message : String? = null, cause : Throwable? = null) : SkriptieError(message, cause)

public class ReferenceError(message : String? = null, cause : Throwable? = null) : SkriptieError(message, cause)

internal fun unresolvedReference(ref : String, obj : String? = null) : Nothing =
    if (obj != null)
        throw ReferenceError("Unresolved reference '$ref' for $obj")
    else throw ReferenceError("$ref is not defined")