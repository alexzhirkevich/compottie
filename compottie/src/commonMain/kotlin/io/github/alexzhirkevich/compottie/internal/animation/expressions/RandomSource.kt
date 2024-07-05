package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.random.Random

internal class RandomSource {

    private var value: Float? = null

    private var random: Random = Random(0)

    private var prevSeed: Int? = null

    fun setSeed(seed: Int, timeless: Boolean) {
        if (prevSeed != seed) {
            random = Random(seed)
            prevSeed = seed
        }

        value = if (timeless) {
            Random.nextFloat()
        } else null
    }

    fun random(): Float {
        return value ?: random.nextFloat()
    }

    fun random(maxValOrArray: Any): Any {
        return when (maxValOrArray) {
            is Number -> value ?: (random.nextFloat() * maxValOrArray.toFloat())
            is Vec2 -> Vec2(
                value ?: (random.nextFloat() * maxValOrArray.x),
                value ?: (random.nextFloat() * maxValOrArray.y),
            )

            else -> error("Can't use $maxValOrArray as a random() parameter")
        }
    }

    fun random(maxValOrArray1: Any, maxValOrArray2: Any): Any {
        return when {
            maxValOrArray1 is Number && maxValOrArray2 is Number ->
                value ?: (random.nextFloat() * maxValOrArray2.toFloat() + maxValOrArray1.toFloat())

            maxValOrArray1 is Vec2 && maxValOrArray2 is Vec2 -> Vec2(
                value ?: (random.nextFloat() * maxValOrArray1.x + maxValOrArray2.x),
                value ?: (random.nextFloat() * maxValOrArray1.y + maxValOrArray1.y),
            )
            else -> error("Can't use $maxValOrArray1 and $maxValOrArray2 as a random() parameter")
        }
    }
}
