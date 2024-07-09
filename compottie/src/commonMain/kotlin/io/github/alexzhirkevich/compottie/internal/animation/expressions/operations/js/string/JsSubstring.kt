package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class JsSubstring(
    private val string : Expression,
    private val start : Expression,
    private val end : Expression?
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val string = string(property,context,state) as String
        val start = (start(property, context, state) as Number).toInt()
        val end = (end?.invoke(property, context, state) as? Number?)?.toInt() ?: string.length

        return string.substring(start, end)
    }
}