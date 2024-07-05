package expressions.global

import expressions.assertFloatApproxEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test

class MathExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun sin() {
        floatProp.assertFloatApproxEquals("Math.sin(Math.PI/2)", 1f)
        floatProp.assertFloatApproxEquals("Math.cos(Math.PI)", -1f)
        floatProp.assertFloatApproxEquals("Math.sqrt(16)", 4f)
    }
}