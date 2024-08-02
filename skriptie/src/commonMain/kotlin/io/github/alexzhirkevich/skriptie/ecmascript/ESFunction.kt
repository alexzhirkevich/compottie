package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.common.Callable

internal interface ESFunction : ESObject, Callable {
    override val type: String
        get() = "function"
}

internal abstract class ESFunctionBase(
    name : String
) : ESObjectBase(name), ESFunction {
    override val type: String
        get() = "function"

    override fun toString(): String {
        return "function $name() { [native code] }"
    }
}