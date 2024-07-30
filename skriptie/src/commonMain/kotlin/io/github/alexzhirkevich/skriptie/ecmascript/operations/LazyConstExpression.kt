package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class LazyConstExpression(
    val init: Expression
) : Expression {

    private var value : Any? = null
    private var initialized : Boolean = false

    override fun invoke(
        context: ScriptContext
    ): Any {
        if (initialized){
            return value!!
        }

        value = init(context)
        initialized = true
        return value!!
    }
}