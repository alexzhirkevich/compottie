package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.SyntaxError
import io.github.alexzhirkevich.skriptie.common.TypeError
import io.github.alexzhirkevich.skriptie.common.unresolvedReference


public enum class VariableType {
    Global, Local, Const
}

public interface ScriptRuntime : LangContext {

    public operator fun contains(variable: String): Boolean

    public operator fun get(variable: String): Any?

    public fun set(variable: String, value: Any?, type: VariableType?)

    public fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any?>> = emptyMap(),
        block: (ScriptRuntime) -> Any?
    ): Any?

    public fun reset()
}

private class BlockScriptContext(
    private val parent : ScriptRuntime
) : DefaultRuntime(), LangContext by parent {

    override fun get(variable: String): Any? {
        return if (variable in variables) {
            super.get(variable)
        } else {
            parent.get(variable)
        }
    }

    override fun contains(variable: String): Boolean {
        return super.contains(variable) || parent.contains(variable)
    }

    override fun set(variable: String, value: Any?, type: VariableType?) {
        when {
            type == VariableType.Global -> parent.set(variable, value, type)
            type != null || variable in variables -> super.set(variable, value, type)
            else -> parent.set(variable, value, type)
        }
    }
}

public abstract class DefaultRuntime : ScriptRuntime {

    protected val variables: MutableMap<String, Pair<VariableType, Any?>> = mutableMapOf()

    private val child by lazy {
        BlockScriptContext(this)
    }

    override fun contains(variable: String): Boolean {
        return variable in variables
    }

    override fun set(variable: String, value: Any?, type: VariableType?) {
        if (type == null && variable !in variables) {
            unresolvedReference(variable)
        }
        if (type != null && variable in variables) {
            throw SyntaxError("Identifier '$variable' is already declared")
        }
        if (type == null && variables[variable]?.first == VariableType.Const) {
            throw TypeError("Assignment to constant variable ('$variable')")
        }
        variables[variable] = (type ?: variables[variable]?.first)!! to value
    }

    override fun get(variable: String): Any? {
        return variables[variable]?.second
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
