package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
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
        context: ScriptContext
    ): Any {
        return if (time == null) {
            wiggle(property, context, state)
        } else {
            state.onTime((time.invoke(context) as Number).toFloat()) {
                wiggle(property, context, it)
            }
        }
    }


    private fun wiggle(
        property: RawProperty<Any>,
        context: ScriptContext,
        state: AnimationState
    ): Any {
        return OpWiggle.invoke(
            property = OpGetTime::invoke,
            freq = (freq(context) as Number).toFloat(),
            amp = (amp(context) as Number).toFloat(),
            octaves = (octaves?.invoke(context) as Number?)?.toInt(),
            ampMult = (ampMult?.invoke(context) as Number?)?.toFloat(),
            time = (time?.invoke(context) as? Number)?.toFloat(),
            state = state,
            lastChange = lastChange,
            wiggle = wiggle,
            prevWigle = prevWigle
        )
    }
}