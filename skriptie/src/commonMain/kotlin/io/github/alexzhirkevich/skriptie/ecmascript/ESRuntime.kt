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
        set("Infinity", Double.POSITIVE_INFINITY, VariableType.Const)
        set("NaN", Double.NaN, VariableType.Const)
        set("undefined", Unit, VariableType.Const)
    }

    final override fun get(variable: String): Any? {
        if (variable in this){
            return super.get(variable)
        }

        val globalThis = get("globalThis") as? ESObject? ?: return super.get(variable)

        if (variable in globalThis){
            return globalThis[variable]
        }

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