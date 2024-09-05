package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.SyntaxError
import io.github.alexzhirkevich.skriptie.common.TypeError


public enum class VariableType {
    Global, Local, Const
}

public interface ScriptRuntime : LangContext {

    public val io : ScriptIO

    public val comparator : Comparator<Any?>

    public operator fun contains(variable: Any?): Boolean

    public operator fun get(variable: Any?): Any?

    public fun set(variable: Any?, value: Any?, type: VariableType?)

    public fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any?>> = emptyMap(),
        block: (ScriptRuntime) -> Any?
    ): Any?

    public fun reset()
}

private class ScopedRuntime(
    private val parent : ScriptRuntime
) : DefaultRuntime(), LangContext by parent {

    override val io: ScriptIO
        get() = parent.io

    override val comparator: Comparator<Any?>
        get() = parent.comparator

    override fun get(variable: Any?): Any? {
        return if (variable in variables) {
            super.get(variable)
        } else {
            parent.get(variable)
        }
    }

    override fun contains(variable: Any?): Boolean {
        return super.contains(variable) || parent.contains(variable)
    }

    override fun set(variable: Any?, value: Any?, type: VariableType?) {
        when {
            type == VariableType.Global -> parent.set(variable, value, type)
            type != null || variable in variables -> super.set(variable, value, type)
            else -> parent.set(variable, value, type)
        }
    }
}

public abstract class DefaultRuntime : ScriptRuntime {

    protected val variables: MutableMap<Any?, Pair<VariableType, Any?>> = mutableMapOf()

    private val child by lazy {
        ScopedRuntime(this)
    }

    override fun contains(variable: Any?): Boolean {
        return variable in variables
    }

    override fun set(variable: Any?, value: Any?, type: VariableType?) {
        if (type != null && variable in variables) {
            throw SyntaxError("Identifier '$variable' is already declared")
        }
        if (type == null && variables[variable]?.first == VariableType.Const) {
            throw TypeError("Assignment to constant variable ('$variable')")
        }
        variables[variable] = (type ?: variables[variable]?.first ?: VariableType.Global) to value
    }

    override fun get(variable: Any?): Any? {
        return if (contains(variable))
            variables[variable]?.second
        else Unit
    }

    final override fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any?>>,
        block: (ScriptRuntime) -> Any?
    ): Any? {
        child.reset()
        extraVariables.forEach { (n, v) ->
            child.set(n, v.second, v.first)
        }
        return block(child)
    }

    override fun reset(){
        variables.clear()
    }
}
