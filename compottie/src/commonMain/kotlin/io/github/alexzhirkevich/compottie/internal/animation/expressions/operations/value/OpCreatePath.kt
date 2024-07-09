package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpPropertyContext
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier

private val DefaultPoints by lazy {
    listOf(
        listOf(0f, 0f),
        listOf(100f, 0f),
        listOf(100f, 100f),
        listOf(0f, 100f)
    )
}

internal class OpCreatePath(
    private val points : Expression?,
    private val inTangents : Expression?,
    private val outTangents : Expression?,
    private val isClosed : Expression?,
) : OpPropertyContext(), Expression {

    init {
        Compottie.logger?.warn("Animation contains 'createPath' expression. It is supported but can cause significant performance drops. If you notice performance issues set enableExpressions=false for Painter")
    }

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {

        val points = points?.invoke(property, context, state) as List<List<Float>>?
            ?: DefaultPoints

        val inTangents = inTangents?.invoke(property, context, state) as List<List<Float>>?
            ?: emptyList()

        val outTangents = outTangents?.invoke(property, context, state) as List<List<Float>>?
            ?: emptyList()

        val isClosed = isClosed?.invoke(property, context, state) as Boolean? ?: true

        return AnimatedShape.Default(
            bezier = Bezier(
                vertices = points,
                inTangents = inTangents,
                outTangents = outTangents,
                isClosed = isClosed
            )
        )
    }
}