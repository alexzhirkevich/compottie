package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test


internal class ClampExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun dot() {
        floatProp.assertValueEquals("clamp(5, 0, 10)", 5f)
        floatProp.assertValueEquals("clamp(-5, 0, 10)", 0f)
        floatProp.assertValueEquals("clamp(0, -10, 10)", 0f)
        floatProp.assertValueEquals("clamp(-15, -10,10)", -10f)
        floatProp.assertValueEquals("clamp(15, -10,10)", 10f)
    }
}