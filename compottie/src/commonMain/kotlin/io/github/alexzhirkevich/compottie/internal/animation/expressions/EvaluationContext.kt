package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface EvaluationContext {

    val variables : MutableMap<String, Any>

    val randomSource : RandomSource
}

internal class DefaultEvaluatorContext(
    override val variables: MutableMap<String, Any> = mutableMapOf(),
    override val randomSource: RandomSource = RandomSource(),
) : EvaluationContext {

    val result: Any? get() = variables["\$bm_rt"]

    fun reset() {
        variables.clear()
    }
}