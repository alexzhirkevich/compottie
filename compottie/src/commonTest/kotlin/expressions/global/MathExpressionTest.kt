package expressions.global

import expressions.testFloatApprox
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test

class MathExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun sin() {
        floatProp.testFloatApprox("Math.sin(Math.PI/2)", 1f)
        floatProp.testFloatApprox("Math.cos(Math.PI)", -1f)
        floatProp.testFloatApprox("Math.sqrt(16)", 4f)
    }
}