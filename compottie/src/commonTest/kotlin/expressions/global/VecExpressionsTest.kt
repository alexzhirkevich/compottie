package expressions.global

import expressions.assertExprReturns
import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.hypot
import kotlin.test.Ignore
import kotlin.test.Test


internal class VecExpressionsTest {

    @Test
    fun dot() {
        "dot([5,6], [7,8])".assertExprValueEquals(83f)
        "dot([-15,6], [7, (8+20)])".assertExprValueEquals(63f)
    }

    @Test
    fun length() {
        "length([5,6])".assertExprValueEquals(hypot(5f, 6f))
        "length([5,6], [7,8])".assertExprValueEquals(hypot(5f - 7, 6f - 8f))
    }

    @Test
    fun normalize() {
        "normalize([5,6])".assertExprValueEquals(Vec2(5f, 6f) / hypot(5f, 6f))
    }
}

