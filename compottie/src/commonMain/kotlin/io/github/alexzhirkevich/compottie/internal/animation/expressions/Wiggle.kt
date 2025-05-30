package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.geometry.Offset
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.timeSeconds
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.js.JsAny
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

internal fun JSWiggle(
    property: RawProperty<*>
) : Callable {
    val lastChange: MutableMap<Int, Long> = mutableMapOf()
    val wiggle: MutableMap<Int, Any> = mutableMapOf()
    val prevWiggle: MutableMap<Int, Any> = mutableMapOf()
    return Callable {
        val freq = it[0]?.toKotlin(this) as Number
        val amp = it[1]?.toKotlin(this) as Number
        val octaves = it.getOrNull(2)?.toKotlin(this) as? Number ?: 1
        val ampMult = it.getOrNull(3)?.toKotlin(this) as? Number ?: 0.5f
        val t = it.getOrNull(4)

        val time = onTime(t){
            wiggle(
                value = it.timeSeconds,
                freq = freq.toFloat(),
                amp = amp.toFloat(),
                octaves = octaves.toInt(),
                ampMult = ampMult.toFloat(),
                state = it,
                lastChange = lastChange, wiggle = wiggle, prevWiggle = prevWiggle
            )
        }

        onTime(time){ property.raw(it) }.toJs()
    }
}

internal fun JSTemporalWiggle(
    property: RawProperty<*>
) : Callable {
    val lastChange: MutableMap<Int, Long> = mutableMapOf()
    val wiggle: MutableMap<Int, Any> = mutableMapOf()
    val prevWiggle: MutableMap<Int, Any> = mutableMapOf()
    return Callable {
        val freq = it[0]?.toKotlin(this) as Number
        val amp = it[1]?.toKotlin(this) as Number
        val octaves = it.getOrNull(2)?.toKotlin(this) as? Number ?: 1
        val ampMult = it.getOrNull(3)?.toKotlin(this) as? Number ?: 0.5f
        val t = it.getOrNull(4)


        onTime(t){
            wiggle(
                value = property.raw(it),
                freq = freq.toFloat(),
                amp = amp.toFloat(),
                octaves = octaves.toInt(),
                ampMult = ampMult.toFloat(),
                state = it,
                lastChange = lastChange,
                wiggle = wiggle,
                prevWiggle = prevWiggle
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
    prevWiggle: MutableMap<Int, Any>
) : JsAny? {

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
                    prevWiggle[it] = (wiggle[it] as? Float) ?: 0f
                    wiggle[it] = -octAmp + Random.nextFloat() * 2 * octAmp
                }

                is Vec2 -> {
                    prevWiggle[it] = (wiggle[it] as? Vec2) ?: Vec2.Zero
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

        val p = prevWiggle[it]
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
    return value.toJs()
}
