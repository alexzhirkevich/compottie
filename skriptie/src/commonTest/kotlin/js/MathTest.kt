package js

import kotlin.test.Test

class MathTest {

    @Test
    fun math() {
        "Math.sin(Math.PI/2)".eval().assertEqualsTo(1.0)
        "Math.sin(Math.PI)".eval().assertEqualsTo(0.0)
        "Math.sin(0)".eval().assertEqualsTo(0.0)
        "Math.sin(0.0)".eval().assertEqualsTo(0.0)

        "Math.cos(Math.PI)".eval().assertEqualsTo(-1.0)
        "Math.cos(0)".eval().assertEqualsTo(1.0)
        "Math.cos(0.0)".eval().assertEqualsTo(1.0)

        "Math.sqrt(16)".eval().assertEqualsTo(4.0)
        "Math.sqrt(16.0)".eval().assertEqualsTo(4.0)

        "Math.imul(3,4)".eval().assertEqualsTo(12L)
        "Math.imul(-5,12)".eval().assertEqualsTo(-60L)
        "Math.imul(0xffffffff, 5)".eval().assertEqualsTo(-5L)
        "Math.imul(0xfffffffe, 5)".eval().assertEqualsTo(-10L)

        "Math.hypot(3, 4)".eval().assertEqualsTo(5.0)
        "Math.hypot(5, 12)".eval().assertEqualsTo(13.0)
        "Math.hypot(3, 4, 5)".eval().assertEqualsTo(7.071068)
        "Math.hypot(-5)".eval().assertEqualsTo(5.0)
    }
}