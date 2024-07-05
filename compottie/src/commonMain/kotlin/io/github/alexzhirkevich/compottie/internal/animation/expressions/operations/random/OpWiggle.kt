package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp as vecLerp
import androidx.compose.ui.util.lerp as floatLerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

internal class OpWiggle(
    private val freq : Expression,
    private val amp : Expression,
    private val octaves : Expression? = null,
    private val ampMult : Expression? = null,
//    private val t : Expression? = null,
) : Expression {

    private var lastChange: MutableMap<Int, Long> = mutableMapOf()
    private var wiggle: MutableMap<Int, Any> = mutableMapOf()
    private var prevWigle: MutableMap<Int, Any> = mutableMapOf()

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        val freq = (freq(property, context, state) as Number).toFloat()
        val amp = (amp(property, context, state) as Number).toFloat()
        val octaves = (octaves?.invoke(property, context, state) as Number?)?.toInt() ?: 1
        val ampMult = (ampMult?.invoke(property, context, state) as Number?)?.toFloat() ?: .5f

        var value = property.raw(state)

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
                value is Float && p is Float && c is Float -> value += floatLerp(p, c, progress)
                value is Vec2 && p is Vec2 && c is Vec2 -> value += vecLerp(p, c, progress)
                else -> error("${value::class} can't be wiggled")
            }
        }
        return value
    }
}