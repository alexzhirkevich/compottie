package js

import kotlin.test.Test

class MathTest {

    @Test
    fun math() {
        "Math.sin(Math.PI/2)".runJs().assertEqualsTo(1.0)
        "Math.sin(Math.PI)".runJs().assertEqualsTo(0.0)
        "Math.sin(0)".runJs().assertEqualsTo(0.0)
        "Math.sin(0.0)".runJs().assertEqualsTo(0.0)

        "Math.cos(Math.PI)".runJs().assertEqualsTo(-1.0)
        "Math.cos(0)".runJs().assertEqualsTo(1.0)
        "Math.cos(0.0)".runJs().assertEqualsTo(1.0)

        "Math.sqrt(16)".runJs().assertEqualsTo(4.0)
        "Math.sqrt(16.0)".runJs().assertEqualsTo(4.0)

        "Math.imul(3,4)".runJs().assertEqualsTo(12L)
        "Math.imul(-5,12)".runJs().assertEqualsTo(-60L)
        "Math.imul(0xffffffff, 5)".runJs().assertEqualsTo(-5L)
        "Math.imul(0xfffffffe, 5)".runJs().assertEqualsTo(-10L)

        "Math.hypot(3, 4)".runJs().assertEqualsTo(5.0)
        "Math.hypot(5, 12)".runJs().assertEqualsTo(13.0)
        "Math.hypot(3, 4, 5)".runJs().assertEqualsTo(7.071068)
        "Math.hypot(-5)".runJs().assertEqualsTo(5.0)
    }
}