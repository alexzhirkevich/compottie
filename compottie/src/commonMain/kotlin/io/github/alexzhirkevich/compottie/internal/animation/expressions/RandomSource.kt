package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.min
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
            is List<*> -> {
                return maxValOrArray.map {
                    (it as Number).toFloat() * randomInstance.nextFloat()
                }
            }

            else -> error("Can't use $maxValOrArray as a random() parameter")
        }
    }

    fun random(maxValOrArray1: Any, maxValOrArray2: Any): Any {
        return when {
            maxValOrArray1 is Number && maxValOrArray2 is Number ->
                (randomInstance.nextFloat() * maxValOrArray2.toFloat() + maxValOrArray1.toFloat())

            maxValOrArray1 is List<*> && maxValOrArray2 is List<*> -> {
                List(min(maxValOrArray1.size, maxValOrArray2.size)){
                    randomInstance.nextFloat() * (maxValOrArray1[it] as Number).toFloat() +
                            (maxValOrArray2[it] as Number).toFloat()
                }
            }

            else -> error("Can't use $maxValOrArray1 and $maxValOrArray2 as a random() parameter")
        }
    }

    fun gaussRandom() : Float {
        val r = sqrt(-2 * ln(random()))
        val alpha = 2 * PI * random()
        return r * cos(alpha).toFloat() + 1
    }

    fun gaussRandom(maxValOrArray: Any): Any {
        return when (maxValOrArray) {
            is Number -> (gaussRandom() * maxValOrArray.toFloat())
            is List<*> -> {
                maxValOrArray as List<Number>
                buildList(maxValOrArray.size){
                    while (this.size < maxValOrArray.size){
                        val r = sqrt(-2 * ln(randomInstance.nextFloat()))
                        val alpha = 2 * PI * randomInstance.nextFloat()

                        add((r * cos(alpha).toFloat() + 1) * maxValOrArray[size].toFloat())
                        add((r * sin(alpha).toFloat() + 1 * maxValOrArray[size].toFloat()))
                    }
                    if (this.size > maxValOrArray.size){
                        removeLast()
                    }
                }
            }

            else -> error("Can't use $maxValOrArray as a random() parameter")
        }
    }

    fun gaussRandom(maxValOrArray1: Any, maxValOrArray2: Any): Any {
        return when {
            maxValOrArray1 is Number && maxValOrArray2 is Number ->
                (gaussRandom() * maxValOrArray2.toFloat() + maxValOrArray1.toFloat())

            maxValOrArray1 is List<*> && maxValOrArray2 is List<*> -> {
                maxValOrArray1 as List<Number>
                maxValOrArray2 as List<Number>
                val cap =min(maxValOrArray1.size, maxValOrArray2.size)
                buildList(cap){
                    while (this.size < cap){
                        val r = sqrt(-2 * ln(randomInstance.nextFloat()))
                        val alpha = 2 * PI * randomInstance.nextFloat()

                        add((r * cos(alpha).toFloat() + 1) * maxValOrArray1[size].toFloat() + maxValOrArray2[size].toFloat())
                        add((r * sin(alpha).toFloat() + 1) * maxValOrArray1[size].toFloat() + maxValOrArray2[size].toFloat())
                    }
                    if (this.size > cap){
                        removeLast()
                    }
                }
            }

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


