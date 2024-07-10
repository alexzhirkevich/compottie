package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class JsReplace(
    private val string : Expression,
    private val pattern : Expression,
    private val replacement : Expression,
    private val all : Boolean
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val string = string(property, context, state) as String
        val pattern = pattern(property, context, state) as String
        val replacement = replacement(property, context, state) as String

        return if (pattern.isEmpty()) {
            replacement + string
        } else {
            if (all) {
                string.replace(pattern, replacement)
            } else {
                string.replaceFirst(pattern, replacement)
            }
        }
    }
}