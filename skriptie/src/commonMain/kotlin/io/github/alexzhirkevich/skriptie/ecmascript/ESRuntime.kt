package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.DefaultRuntime
import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.common.TypeError

public abstract class ESRuntime : DefaultRuntime(), ESObject {

    init {
        init()
    }

    override fun reset() {
        super.reset()
        init()
    }

    private fun init() {
        set("Number", ESNumber(), VariableType.Const)
        set("globalThis", this, VariableType.Const)
    }

    final override fun get(variable: String): Any? {
        return super.get(variable)
    }

    final override fun set(variable: String, value: Any?) {
        set(variable, value, VariableType.Local)
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        throw TypeError("ScriptRuntime is not a function")
    }
}