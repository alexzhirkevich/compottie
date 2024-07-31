package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.common.SyntaxError
import io.github.alexzhirkevich.skriptie.common.TypeError
import io.github.alexzhirkevich.skriptie.common.unresolvedReference


public enum class VariableType {
    Global, Local, Const
}

public interface ScriptContext {

    public fun hasVariable(name: String): Boolean

    public fun getVariable(name: String): Any?

    public fun setVariable(name: String, value: Any?, type: VariableType?)

    public fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any?>> = emptyMap(),
        block: (ScriptContext) -> Any?
    ): Any?
    public fun reset()
}

private class BlockScriptContext(
    private val parent : ScriptContext
) : EcmascriptContext() {

    override fun getVariable(name: String): Any? {
        return if (name in variables) {
            super.getVariable(name)
        } else {
            parent.getVariable(name)
        }
    }

    override fun hasVariable(name: String): Boolean {
        return super.hasVariable(name) || parent.hasVariable(name)
    }

    override fun setVariable(name: String, value: Any?, type: VariableType?) {
        when {
            type == VariableType.Global -> parent.setVariable(name, value, type)
            type != null || name in variables -> super.setVariable(name, value, type)
            else -> parent.setVariable(name, value, type)
        }
    }
}

public abstract class EcmascriptContext : ScriptContext {

    protected val variables: MutableMap<String, Pair<VariableType, Any?>> = mutableMapOf()

    private val child by lazy {
        BlockScriptContext(this)
    }

    override fun hasVariable(name: String): Boolean {
        return name in variables
    }

    override fun setVariable(name: String, value: Any?, type: VariableType?) {
        if (type == null && name !in variables) {
            unresolvedReference(name)
        }
        if (type != null && name in variables) {
            throw SyntaxError("Identifier '$name' is already declared")
        }
        if (type == null && variables[name]?.first == VariableType.Const) {
            throw TypeError("Assignment to constant variable ('$name')")
        }
        variables[name] = (type ?: variables[name]?.first)!! to value
    }

    override fun getVariable(name: String): Any? {
        return variables[name]?.second
    }

    final override fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any?>>,
        block: (ScriptContext) -> Any?
    ): Any? {
        child.reset()
        extraVariables.forEach { (n, v) ->
            child.setVariable(n, v.second, v.first)
        }
        return block(child)
    }

    override fun reset(){
        variables.clear()
    }
}
