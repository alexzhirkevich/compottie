package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class JsEndsWith(
    private val string : Expression,
    private val searchString : Expression,
    private val position : Expression?
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Boolean {
        val string = string(property,context,state) as String
        val searchString = searchString(property, context, state) as String
        val position = position?.invoke(property, context, state) as? Number?

        return if (position == null){
            string.endsWith(searchString)
        } else {
            string.take(position.toInt()).endsWith(searchString)
        }
    }
}