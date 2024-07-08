package expressions.global

import expressions.assertExprValueEquals
import kotlin.math.PI
import kotlin.test.Test

internal class MathExpressionTest {

    @Test
    fun math() {
        "Math.sin(Math.PI/2)".assertExprValueEquals(1f)
        "Math.sin(Math.PI)".assertExprValueEquals(0f)
        "Math.sin(0)".assertExprValueEquals(0f)
        "Math.sin(0.0)".assertExprValueEquals(0f)
        "Math.cos(Math.PI)".assertExprValueEquals(-1f)
        "Math.cos(0)".assertExprValueEquals(1f)
        "Math.cos(0.0)".assertExprValueEquals(1f)
        "Math.sqrt(16)".assertExprValueEquals(4f)
        "Math.sqrt(16.0)".assertExprValueEquals(4f)
        "radiansToDegrees(Math.PI)".assertExprValueEquals(180f)
        "radiansToDegrees(-Math.PI)".assertExprValueEquals(-180f)
        "degreesToRadians(90)".assertExprValueEquals(PI.toFloat()/2)
        "degreesToRadians(-180)".assertExprValueEquals(-PI.toFloat())
    }
}