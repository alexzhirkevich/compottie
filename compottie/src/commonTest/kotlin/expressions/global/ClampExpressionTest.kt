package expressions.global

import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test


internal class ClampExpressionTest {

    @Test
    fun clamp() {
        "clamp(5, 0, 10)".assertExprValueEquals(5f)
        "clamp(-5, 0, 10)".assertExprValueEquals(0f)
        "clamp(0, -10, 10)".assertExprValueEquals(0f)
        "clamp(-15, -10,10)".assertExprValueEquals(-10f)
        "clamp(15, -10,10)".assertExprValueEquals(10f)
    }
}