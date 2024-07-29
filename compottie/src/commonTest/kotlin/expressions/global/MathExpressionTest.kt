package expressions.global

import expressions.assertExprEquals
import expressions.assertSimpleExprEquals
import kotlin.math.PI
import kotlin.test.Test

class MathExpressionTest {

    @Test
    fun math() {
        "Math.sin(Math.PI/2)".assertSimpleExprEquals(1.0)
        "Math.sin(Math.PI)".assertSimpleExprEquals(0.0)
        "Math.sin(0)".assertSimpleExprEquals(0.0)
        "Math.sin(0.0)".assertSimpleExprEquals(0.0)

        "Math.cos(Math.PI)".assertSimpleExprEquals(-1.0)
        "Math.cos(0)".assertSimpleExprEquals(1.0)
        "Math.cos(0.0)".assertSimpleExprEquals(1.0)

        "Math.sqrt(16)".assertSimpleExprEquals(4.0)
        "Math.sqrt(16.0)".assertSimpleExprEquals(4.0)

        "radiansToDegrees(Math.PI)".assertSimpleExprEquals(180.0)
        "radiansToDegrees(-Math.PI)".assertSimpleExprEquals(-180.0)

        "degreesToRadians(90)".assertExprEquals(PI.toFloat()/2)
        "degreesToRadians(-180)".assertExprEquals(-PI.toFloat())

        "Math.imul(3,4)".assertSimpleExprEquals(12L)
        "Math.imul(-5,12)".assertSimpleExprEquals(-60L)
        "Math.imul(0xffffffff, 5)".assertSimpleExprEquals(-5L)
        "Math.imul(0xfffffffe, 5)".assertSimpleExprEquals(-10L)

        "Math.hypot(3, 4)".assertSimpleExprEquals(5.0)
        "Math.hypot(5, 12)".assertSimpleExprEquals(13.0)
        "Math.hypot(3, 4, 5)".assertSimpleExprEquals(7.071068)
        "Math.hypot(-5)".assertSimpleExprEquals(5.0)
    }
}