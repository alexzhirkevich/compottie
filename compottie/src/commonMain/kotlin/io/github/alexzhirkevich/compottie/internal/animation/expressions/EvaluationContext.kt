package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpFunction
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference


internal enum class VariableScope {
    Global, Block
}

internal interface EvaluationContext {

    val randomSource : RandomSource

    fun registerFunction(function: OpFunction)

    fun getFunction(name: String) : OpFunction?

    fun getVariable(name : String) : Any?

    fun setVariable(name: String, value : Any, scope: VariableScope?)

    fun withScope(extraVariables : Map<String ,Any>, block : (EvaluationContext) -> Any) : Any

}

internal class DefaultEvaluatorContext(
    override val randomSource: RandomSource = RandomSource(),
) : EvaluationContext {

    private val globalVariables: MutableMap<String, Any> = mutableMapOf()

    private val blockVariables: MutableMap<String, Any> = mutableMapOf()

    private val functions: MutableMap<String, OpFunction> = mutableMapOf()

    private val child by lazy {
        BlockEvaluatorContext(this)
    }

    val result: Any? get() = getVariable("\$bm_rt")

    fun reset() {
        globalVariables.clear()
        blockVariables.clear()
    }

    override fun setVariable(name: String, value: Any, scope: VariableScope?) {
        val map = when {
            scope == VariableScope.Global -> globalVariables
            scope == VariableScope.Block -> blockVariables
            name in blockVariables -> blockVariables
            name in globalVariables -> globalVariables
            else -> unresolvedReference(name)
        }
        map[name] = value
    }

    override fun registerFunction(function: OpFunction) {
        functions[function.name] = function
    }

    override fun getFunction(name: String): OpFunction? {
        return functions[name]
    }

    override fun getVariable(name: String): Any? {
        return when {
            blockVariables.containsKey(name) -> blockVariables[name]
            globalVariables.containsKey(name) -> globalVariables[name]
            else -> null
        }
    }

    override fun withScope(
        extraVariables: Map<String, Any>,
        block: (EvaluationContext) -> Any
    ) : Any {
        child.reset()
        extraVariables.forEach { (n, v) ->
            child.setVariable(n, v, VariableScope.Block)
        }
        return block(child)
    }
}

private class BlockEvaluatorContext(
   private val parent : EvaluationContext
) : EvaluationContext {

    private val child by lazy {
        BlockEvaluatorContext(this)
    }

    private val scopeVariables: MutableMap<String, Any> = mutableMapOf()
    private val scopeFunctions: MutableMap<String, OpFunction> = mutableMapOf()

    override val randomSource: RandomSource
        get() = parent.randomSource

    override fun registerFunction(function: OpFunction) {
        scopeFunctions[function.name] = function
    }

    override fun getFunction(name: String): OpFunction? {
        return scopeFunctions[name] ?: parent.getFunction(name)
    }

    override fun getVariable(name: String): Any? {
        return if (scopeVariables.containsKey(name)) {
            scopeVariables[name]
        } else {
            parent.getVariable(name)
        }
    }

    override fun setVariable(name: String, value: Any, scope: VariableScope?) {
        when {
            scope == VariableScope.Global -> parent.setVariable(name, value, scope)
            scope == VariableScope.Block || name in scopeVariables -> scopeVariables[name] = value
            else -> parent.setVariable(name, value, scope)
        }
    }

    override fun withScope(extraVariables: Map<String, Any>, block: (EvaluationContext) -> Any) : Any {
        child.reset()
        extraVariables.forEach { (n, v) ->
            child.setVariable(n, v, VariableScope.Block)
        }
        return block(child)
    }

    fun reset() {
        scopeFunctions.clear()
        scopeVariables.clear()
    }
}