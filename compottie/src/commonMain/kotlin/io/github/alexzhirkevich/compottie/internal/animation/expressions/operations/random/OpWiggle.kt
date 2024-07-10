package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp as vecLerp
import androidx.compose.ui.util.lerp as floatLerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

internal class OpWiggle(
    private val property: Expression,
    private val freq : Expression,
    private val amp : Expression,
    private val octaves : Expression? = null,
    private val ampMult : Expression? = null,
    private val time : Expression? = null,
) : Expression {

    private var lastChange: MutableMap<Int, Long> = mutableMapOf()
    private var wiggle: MutableMap<Int, Any> = mutableMapOf()
    private var prevWigle: MutableMap<Int, Any> = mutableMapOf()


    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val t = time.takeIf { it !is OpGetTime }
       return if (t == null) {
           wiggle(property, context, state)
       } else {
           state.onTime((t(property, context, state) as Number).toFloat()){
               wiggle(property, context, it)
           }
       }
    }


    private fun wiggle(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ) : Any {
        val prop = property(property, context, state) as RawProperty<Any>

        return invoke(
            property = prop::raw,
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

    companion object {

        fun invoke(
            property: (AnimationState) -> Any,
            freq : Float,
            amp : Float,
            octaves : Int?,
            ampMult : Float?,
            time : Float?,
            state: AnimationState,
            lastChange: MutableMap<Int, Long>,
            wiggle: MutableMap<Int, Any>,
            prevWigle: MutableMap<Int, Any>
        ) : Any {
            return if (time == null) {
                wiggle(
                    value = property(state),
                    freq = freq,
                    amp = amp,
                    octaves = octaves ?: 1,
                    ampMult = ampMult ?: .5f,
                    state = state,
                    lastChange = lastChange,
                    wiggle = wiggle,
                    prevWigle = prevWigle
                )
            } else {
                state.onTime(time) {
                    wiggle(
                        value = property(it),
                        freq = freq,
                        amp = amp,
                        octaves = octaves ?: 1,
                        ampMult = ampMult ?: .5f,
                        state = it,
                        lastChange = lastChange,
                        wiggle = wiggle,
                        prevWigle = prevWigle
                    )
                }
            }
        }

        private fun wiggle(
            value: Any,
            freq : Float,
            amp : Float,
            octaves : Int,
            ampMult : Float,
            state: AnimationState,
            lastChange: MutableMap<Int, Long>,
            wiggle: MutableMap<Int, Any>,
            prevWigle: MutableMap<Int, Any>
        ) : Any {

            var value = value

            repeat(octaves) {
                val octAmp = amp / (if (it == 0) 1f else ampMult.pow(it))
                val octFreq = freq * (if (it == 0) 1f else ampMult.pow(it))

                val octLast = lastChange[it]

                val frameTime = 1000f / octFreq
                val elapsedTime = abs(state.time.inWholeMilliseconds - (octLast ?: 0)).toFloat()

                val progress = if (octLast == null || elapsedTime > frameTime) {
                    lastChange[it] = state.time.inWholeMilliseconds

                    when (value) {
                        is Float -> {
                            prevWigle[it] = (wiggle[it] as? Float) ?: 0f
                            wiggle[it] = -octAmp + Random.nextFloat() * 2 * octAmp
                        }

                        is Vec2 -> {
                            prevWigle[it] = (wiggle[it] as? Vec2) ?: Vec2.Zero
                            wiggle[it] = Offset(
                                -octAmp + Random.nextFloat() * 2 * octAmp,
                                -octAmp + Random.nextFloat() * 2 * octAmp,
                            )
                        }

                        else -> error("${value::class} can't be wiggled")
                    }
                    0f
                } else {
                    (elapsedTime / frameTime).coerceIn(0f, 1f)
                }

                val p = prevWigle[it]
                val c = wiggle[it]

                when {
                    value is Float && p is Float && c is Float -> value += androidx.compose.ui.util.lerp(
                        p,
                        c,
                        progress
                    )
                    value is Vec2 && p is Vec2 && c is Vec2 -> value += androidx.compose.ui.geometry.lerp(
                        p,
                        c,
                        progress
                    )
                    else -> error("${value::class} can't be wiggled")
                }
            }
            return value
        }
    }
}