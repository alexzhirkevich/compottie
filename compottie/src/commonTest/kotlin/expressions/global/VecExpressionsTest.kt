package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.hypot
import kotlin.test.Test


internal class VecExpressionsTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun dot() {
        floatProp.assertValueEquals("dot([5,6], [7,8])", 5 * 7 + 6 * 8f)
        floatProp.assertValueEquals("dot([-15,6], [7, (8+20)])", -15 * 7 + 6 * (8f + 20))
    }

    @Test
    fun length() {
        floatProp.assertValueEquals("length([5,6])", hypot(5f, 6f))
        floatProp.assertValueEquals("length([5,6], [7,8])", hypot(5f - 7, 6f - 8f))
    }

    @Test
    fun normalize() {
        floatProp.assertValueEquals("normalize([5,6])", Vec2(5f, 6f) / hypot(5f, 6f))
    }

}

