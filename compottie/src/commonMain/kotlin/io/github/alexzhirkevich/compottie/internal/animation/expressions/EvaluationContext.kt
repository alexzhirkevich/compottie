package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface EvaluationContext {
    val variables : MutableMap<String, Any>

    val randomSource : RandomSource
}

internal class DefaultEvaluatorContext : EvaluationContext {

    override val variables: MutableMap<String, Any> = mutableMapOf()

    override val randomSource: RandomSource = DefaultRandomSource()

    val result: Any get() = checkNotNull(variables["\$bm_rt"]) {
        "\$bm_rt is null"
    }

    fun reset() {
        variables.clear()
    }
}