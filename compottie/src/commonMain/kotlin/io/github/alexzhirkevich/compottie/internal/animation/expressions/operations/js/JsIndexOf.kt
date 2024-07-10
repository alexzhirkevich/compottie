package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import kotlin.math.pow
import kotlin.math.roundToInt

internal class JsIndexOf(
    private val value : Expression,
    private val search : Expression,
    private val last : Boolean
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val value = value(property, context, state)
        val search = search(property, context, state)

        return when {
            value is String && (search is String || search is Char) -> {
                if (search is String) {
                    if (last)
                        value.lastIndexOf(search)
                    else value.indexOf(search)
                } else {
                    value.indexOf(search as Char)
                }
            }

            value is List<*> -> if(last)
                value.lastIndexOf(search)
            else value.indexOf(search)
            else -> unresolvedReference("indexOf")
        }
    }
}

