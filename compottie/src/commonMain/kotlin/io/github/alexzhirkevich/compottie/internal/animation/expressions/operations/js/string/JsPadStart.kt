package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class JsPadStart(
    private val string : Expression,
    private val targetLength : Expression,
    private val padString : Expression?,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val string = string(property, context, state) as String
        val padString = padString?.invoke(property, context, state) as String? ?: ""
        val targetLength = (targetLength(property, context, state) as Number).toInt()

        val toAppend = targetLength - string.length
        return buildString(targetLength) {
            while (length < toAppend) {
                append(0, padString.take(toAppend - length))
            }
            append(string)
        }
    }
}