package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpFunction
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference


internal enum class VariableType {
    Var, Let, Const
}

internal interface EvaluationContext {

    val random: RandomSource

    fun getVariable(name: String): Any?

    fun setVariable(name: String, value: Any, type: VariableType?)

    fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any>> = emptyMap(),
        block : (EvaluationContext) -> Any
    ) : Any
}

internal class DefaultEvaluatorContext(
    override val random: RandomSource = RandomSource(),
) : BaseEvaluationContext() {

    private val functions: MutableMap<String, OpFunction> = mutableMapOf()

    val result: Any? get() = getVariable("\$bm_rt")

    fun reset() {
        variables.clear()
        functions.clear()
    }
}

private class BlockEvaluatorContext(
    private val parent : EvaluationContext
) : BaseEvaluationContext() {

    override val random: RandomSource
        get() = parent.random

    override fun getVariable(name: String): Any? {
        return if (name in variables) {
            super.getVariable(name)
        } else {
            parent.getVariable(name)
        }
    }

    override fun setVariable(name: String, value: Any, type: VariableType?) {
        when {
            type == VariableType.Var -> parent.setVariable(name, value, type)
            type != null || name in variables -> super.setVariable(name, value, type)
            else -> parent.setVariable(name, value, type)
        }
    }

    fun reset() {
        variables.clear()
    }
}

internal abstract class BaseEvaluationContext : EvaluationContext {

    protected val variables: MutableMap<String, Pair<VariableType, Any>> = mutableMapOf()

    private val child by lazy {
        BlockEvaluatorContext(this)
    }

    override fun setVariable(name: String, value: Any, type: VariableType?) {
        if (type == null && name !in variables) {
            unresolvedReference(name)
        }
        if (type != null && name in variables) {
            error("Identifier '$name' is already declared")
        }
        if (type == null && variables[name]?.first == VariableType.Const) {
            error("TypeError: Assignment to constant variable ('$name')")
        }
        variables[name] = (type ?: variables[name]?.first)!! to value
    }

    override fun getVariable(name: String): Any? {
        return variables[name]?.second
    }

    final override fun withScope(
        extraVariables: Map<String, Pair<VariableType, Any>>,
        block: (EvaluationContext) -> Any
    ) : Any {
        child.reset()
        extraVariables.forEach { (n, v) ->
            child.setVariable(n, v.second, v.first)
        }
        return block(child)
    }

}