package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime

internal class OpTemporalWiggle(
    private val freq: Expression,
    private val amp: Expression,
    private val octaves: Expression? = null,
    private val ampMult: Expression? = null,
    private val time: Expression? = null,
) : Expression {

    private var lastChange: MutableMap<Int, Long> = mutableMapOf()
    private var wiggle: MutableMap<Int, Any> = mutableMapOf()
    private var prevWigle: MutableMap<Int, Any> = mutableMapOf()

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (time == null) {
            wiggle(property, context, state)
        } else {
            state.onTime((time.invoke(property, context, state) as Number).toFloat()) {
                wiggle(property, context, it)
            }
        }
    }


    private fun wiggle(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return OpWiggle.invoke(
            property = OpGetTime::invoke,
            freq = (freq(property, context, state) as Number).toFloat(),
            amp = (amp(property, context, state) as Number).toFloat(),
            octaves = (octaves?.invoke(property, context, state) as Number?)?.toInt(),
            ampMult = (ampMult?.invoke(property, context, state) as Number?)?.toFloat(),
            time = (time?.invoke(property, context, state) as? Number)?.toFloat(),
            state = state,
            lastChange = lastChange,
            wiggle = wiggle,
            prevWigle = prevWigle
        )
    }
}