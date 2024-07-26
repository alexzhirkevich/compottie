package expressions.global

import expressions.assertExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.hypot
import kotlin.test.Test


class VecExpressionsTest {

    @Test
    fun dot() {
        "dot([5,6], [7,8])".assertExprEquals(83f)
        "dot([-15,6], [7, (8+20)])".assertExprEquals(63f)
    }

    @Test
    fun length() {
        "length([5,6])".assertExprEquals(hypot(5f, 6f))
        "length([5,6], [7,8])".assertExprEquals(hypot(5f - 7, 6f - 8f))
    }

    @Test
    fun normalize() {
        "normalize([5,6])".assertExprEquals(Vec2(5f, 6f) / hypot(5f, 6f))
    }
}

