package com.airbnb.lottie.utils

import android.graphics.Path
import android.graphics.PointF
import androidx.annotation.FloatRange
import com.airbnb.lottie.animation.content.KeyPathElementContent
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.model.content.ShapeData
import kotlin.math.max
import kotlin.math.min

object MiscUtils {
    private val pathFromDataCurrentPoint = PointF()

    fun addPoints(p1: PointF, p2: PointF): PointF {
        return PointF(p1.x + p2.x, p1.y + p2.y)
    }

    @JvmStatic
    fun getPathFromData(shapeData: ShapeData, outPath: Path) {
        outPath.reset()
        val initialPoint = shapeData.initialPoint
        outPath.moveTo(initialPoint.x, initialPoint.y)
        pathFromDataCurrentPoint[initialPoint.x] = initialPoint.y
        for (i in shapeData.curves.indices) {
            val curveData = shapeData.curves[i]
            val cp1 = curveData.controlPoint1
            val cp2 = curveData.controlPoint2
            val vertex = curveData.vertex

            if (cp1 == pathFromDataCurrentPoint && cp2 == vertex) {
                // On some phones like Samsung phones, zero valued control points can cause artifacting.
                // https://github.com/airbnb/lottie-android/issues/275
                //
                // This does its best to add a tiny value to the vertex without affecting the final
                // animation as much as possible.
                // outPath.rMoveTo(0.01f, 0.01f);
                outPath.lineTo(vertex.x, vertex.y)
            } else {
                outPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y)
            }
            pathFromDataCurrentPoint[vertex.x] = vertex.y
        }
        if (shapeData.isClosed) {
            outPath.close()
        }
    }

    @JvmStatic
    fun lerp(a: Float, b: Float, percentage: Float): Float {
        return a + percentage * (b - a)
    }

    fun lerp(a: Double, b: Double, percentage: Double): Double {
        return a + percentage * (b - a)
    }

    @JvmStatic
    fun lerp(a: Int, b: Int,  percentage: Float): Int {
        return (a + percentage * (b - a)).toInt()
    }

    fun floorMod(x: Float, y: Float): Int {
        return floorMod(x.toInt(), y.toInt())
    }

    private fun floorMod(x: Int, y: Int): Int {
        return x - y * floorDiv(x, y)
    }

    private fun floorDiv(x: Int, y: Int): Int {
        var r = x / y
        val sameSign = (x xor y) >= 0
        val mod = x % y
        if (!sameSign && mod != 0) {
            r--
        }
        return r
    }

    @JvmStatic
    fun clamp(number: Int, min: Int, max: Int): Int {
        return max(min.toDouble(), min(max.toDouble(), number.toDouble())).toInt()
    }

    @JvmStatic
    fun clamp(number: Float, min: Float, max: Float): Float {
        return max(min.toDouble(), min(max.toDouble(), number.toDouble())).toFloat()
    }

    @JvmStatic
    fun clamp(number: Double, min: Double, max: Double): Double {
        return max(min, min(max, number))
    }

    fun contains(number: Float, rangeMin: Float, rangeMax: Float): Boolean {
        return number >= rangeMin && number <= rangeMax
    }

    /**
     * Helper method for any [KeyPathElementContent] that will check if the content
     * fully matches the keypath then will add itself as the final key, resolve it, and add
     * it to the accumulator list.
     *
     *
     * Any [KeyPathElementContent] should call through to this as its implementation of
     * [KeyPathElementContent.resolveKeyPath].
     */
    @JvmStatic
    fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: MutableList<KeyPath?>,
        currentPartialKeyPath: KeyPath, content: KeyPathElementContent
    ) {
        var currentPartialKeyPath = currentPartialKeyPath
        if (keyPath.fullyResolvesTo(content.name, depth)) {
            currentPartialKeyPath = currentPartialKeyPath.addKey(content.name)
            accumulator.add(currentPartialKeyPath.resolve(content))
        }
    }
}
