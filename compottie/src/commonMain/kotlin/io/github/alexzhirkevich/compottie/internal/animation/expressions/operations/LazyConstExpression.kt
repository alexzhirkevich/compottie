package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class LazyConstExpression(
    val init: Expression
) : Expression {

    private var value : Any? = null
    private var initialized : Boolean = false

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        if (initialized){
            return value!!
        }

        value = init(property, context, state)
        initialized = true
        return value!!
    }
}