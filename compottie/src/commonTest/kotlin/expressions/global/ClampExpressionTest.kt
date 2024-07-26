package expressions.global

import expressions.assertExprEquals
import kotlin.test.Test


class ClampExpressionTest {

    @Test
    fun clamp() {
        "clamp(5, 0, 10)".assertExprEquals(5f)
        "clamp(-5, 0, 10)".assertExprEquals(0f)
        "clamp(0, -10, 10)".assertExprEquals(0f)
        "clamp(-15, -10,10)".assertExprEquals(-10f)
        "clamp(15, -10,10)".assertExprEquals(10f)
    }
}