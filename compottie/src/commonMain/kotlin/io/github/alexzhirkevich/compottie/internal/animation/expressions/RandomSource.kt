package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class RandomSource {

    private var randomInstance: Random = Random
    private val noiseMap = mutableMapOf<Int, Float>()

    fun setSeed(seed: Int, timeless: Boolean) {
        randomInstance = Random(seed)
        noiseMap.clear()
    }

    fun random(): Float {
        return randomInstance.nextFloat()
    }

    fun random(maxValOrArray: Any): Any {
        return when (maxValOrArray) {
            is Number -> (randomInstance.nextFloat() * maxValOrArray.toFloat())
            is Vec2 -> Vec2(
                (randomInstance.nextFloat() * maxValOrArray.x),
                (randomInstance.nextFloat() * maxValOrArray.y),
            )

            else -> error("Can't use $maxValOrArray as a random() parameter")
        }
    }

    fun random(maxValOrArray1: Any, maxValOrArray2: Any): Any {
        return when {
            maxValOrArray1 is Number && maxValOrArray2 is Number ->
                (randomInstance.nextFloat() * maxValOrArray2.toFloat() + maxValOrArray1.toFloat())

            maxValOrArray1 is Vec2 && maxValOrArray2 is Vec2 -> Vec2(
                (randomInstance.nextFloat() * maxValOrArray1.x + maxValOrArray2.x),
                (randomInstance.nextFloat() * maxValOrArray1.y + maxValOrArray1.y),
            )

            else -> error("Can't use $maxValOrArray1 and $maxValOrArray2 as a random() parameter")
        }
    }

    fun gaussRandom() : Float {
        val r = sqrt(-2 * ln(random()))
        val alpha = 2 * PI * random()
        return r * cos(alpha).toFloat() + 1
    }

    fun gaussRandom(maxValOrArray: Any): Any {

        val r = sqrt(-2 * ln(random()))
        val alpha = 2 * PI * random()

        return when (maxValOrArray) {
            is Number -> (gaussRandom() * maxValOrArray.toFloat())
            is Vec2 -> Vec2(
                ((r * cos(alpha).toFloat() + 1) * maxValOrArray.x),
                (r * sin(alpha).toFloat() + 1 * maxValOrArray.y),
            )

            else -> error("Can't use $maxValOrArray as a random() parameter")
        }
    }

    fun gaussRandom(maxValOrArray1: Any, maxValOrArray2: Any): Any {

        val r = sqrt(-2 * ln(random()))
        val alpha = 2 * PI * random()

        return when {
            maxValOrArray1 is Number && maxValOrArray2 is Number ->
                ((r * cos(alpha).toFloat() + 1) * maxValOrArray2.toFloat() + maxValOrArray1.toFloat())

            maxValOrArray1 is Vec2 && maxValOrArray2 is Vec2 -> Vec2(
                ((r * cos(alpha).toFloat() + 1) * maxValOrArray1.x + maxValOrArray2.x),
                ((r * sin(alpha).toFloat() + 1) * maxValOrArray1.y + maxValOrArray1.y),
            )

            else -> error("Can't use $maxValOrArray1 and $maxValOrArray2 as a random() parameter")
        }
    }

    fun noise(t: Float): Float {
        val prevX = t.toInt()
        val nextX = prevX + 1
        val fracX = t - prevX

        val y0 = getOrInitNoise(prevX)
        val y1 = getOrInitNoise(nextX)
        return lerp(y0, y1, fracX)
    }

    private fun getOrInitNoise(x: Int): Float {
        return noiseMap[x] ?: (randomInstance.nextFloat() * 2 - 1).also {
            noiseMap[x] = it
        }
    }
}


