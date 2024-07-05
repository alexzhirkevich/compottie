package expressions.global

import expressions.MockAnimationState
import expressions.ret
import expressions.assertExpressionReturns
import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

internal class RandomExpressionsTest {

    @Test
    fun noise() {
        repeat(3) {
            for (N in listOf(24, 60, 120, 240, 480, 1000, 2000, 5000)) {
                val floatProp = AnimatedNumber.Default(0f)

                val ev = ExpressionEvaluator<Float>("var $ret=noise(time)")

                val random = List(N) {
                    val state = MockAnimationState(it.toFloat(), N.toFloat())
                    ev.run { floatProp.evaluate(state) }
                }

                assertTrue { random.all { it in -1f..1f } }   // in range
                assertTrue { random.toSet().size > random.size * .85f }  // different

                // average diff between 2 near items is low

                val avgDiff = List(random.size - 2) {
                    abs(random[it] - random[it + 1])
                }.average()

                assertTrue { avgDiff < .1f }
            }
        }
    }

    @Test
    fun random() {
        val floatProp = AnimatedNumber.Default(0f)
        val vecProp = AnimatedVector2.Default(listOf(0f,0f))

        floatProp.assertExpressionReturns("seedRandom(0); var $ret=random()", 0.54963315f)
        floatProp.assertExpressionReturns("seedRandom(0); var $ret=random(3)", 1.6488994f)
        vecProp.assertValueEquals("seedRandom(0); var $ret=random([3,4])", Vec2(1.6488994f, 1.3124194f))
    }

    @Test
    fun gauss() {
        val floatProp = AnimatedNumber.Default(0f)
        val vecProp = AnimatedVector2.Default(listOf(0f,0f))

        floatProp.assertExpressionReturns("seedRandom(0); var $ret=gaussRandom()", 0.48437697f)
        floatProp.assertExpressionReturns("seedRandom(0); var $ret=gaussRandom(3)", 2.0921092f)
        vecProp.assertExpressionReturns("seedRandom(0); var $ret=gaussRandom([3,4])", Vec2(1.453131f, 4.9649568f))
    }
}