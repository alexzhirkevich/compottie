package expressions.global

import expressions.assertExprEquals
import kotlin.math.PI
import kotlin.test.Test

class MathExpressionTest {

    @Test
    fun math() {
        "Math.sin(Math.PI/2)".assertExprEquals(1f)
        "Math.sin(Math.PI)".assertExprEquals(0f)
        "Math.sin(0)".assertExprEquals(0f)
        "Math.sin(0.0)".assertExprEquals(0f)

        "Math.cos(Math.PI)".assertExprEquals(-1f)
        "Math.cos(0)".assertExprEquals(1f)
        "Math.cos(0.0)".assertExprEquals(1f)

        "Math.sqrt(16)".assertExprEquals(4f)
        "Math.sqrt(16.0)".assertExprEquals(4f)

        "radiansToDegrees(Math.PI)".assertExprEquals(180f)
        "radiansToDegrees(-Math.PI)".assertExprEquals(-180f)

        "degreesToRadians(90)".assertExprEquals(PI.toFloat()/2)
        "degreesToRadians(-180)".assertExprEquals(-PI.toFloat())

        "Math.imul(3,4)".assertExprEquals(12f)
        "Math.imul(-5,12)".assertExprEquals(-60)
        "Math.imul(0xffffffff, 5)".assertExprEquals(-5)
        "Math.imul(0xfffffffe, 5)".assertExprEquals(-10)

        "Math.hypot(3, 4)".assertExprEquals(5f)
        "Math.hypot(5, 12)".assertExprEquals(13f)
        "Math.hypot(3, 4, 5)".assertExprEquals(7.071068f)
        "Math.hypot(-5)".assertExprEquals(5f)
    }
}