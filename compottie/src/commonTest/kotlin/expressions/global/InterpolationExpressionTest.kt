package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test

internal class InterpolationExpressionTest {

    private val flotProp = AnimatedNumber.Default(100f)

    @Test
    fun linear() {
        flotProp.assertValueEquals("linear(0, 0, 100)", 0)
        flotProp.assertValueEquals("linear(0, 0.0, 100)", 0f)
        flotProp.assertValueEquals("linear(1, 0, 100.0)", 100f)
        flotProp.assertValueEquals("linear(1, 0, 100)", 100)
        flotProp.assertValueEquals("linear(0.5, 0, 100)", 50f)
        flotProp.assertValueEquals("linear(0.5, 0.0, 100)", 50f)

        flotProp.assertValueEquals("linear(1.5, 1, 2, 0, 100)", 50f)

        flotProp.assertValueEquals("linear(1.1, 0,1, 0, 100)", 100)
        flotProp.assertValueEquals("linear(1.1, 0,1, 0, 100.0)", 100f)
        flotProp.assertValueEquals("linear(-0.1, 0,1, 0, 100)", 0)
        flotProp.assertValueEquals("linear(-0.1, 0,1, 0.0, 100)", 0f)


    }
}