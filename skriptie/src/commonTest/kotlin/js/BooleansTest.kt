package js

import kotlin.test.Test

class BooleansTest {

    @Test
    fun and() {
        "true && true".eval().assertEqualsTo(true)
        "true && true && true".eval().assertEqualsTo(true)

        "false && false".eval().assertEqualsTo(false)
        "true && false".eval().assertEqualsTo(false)
        "false && true".eval().assertEqualsTo(false)

        "true && true && false".eval().assertEqualsTo(false)
        "false && true && true".eval().assertEqualsTo(false)
    }

    @Test
    fun or() {
        "true || true".eval().assertEqualsTo(true)
        "true || true || true".eval().assertEqualsTo(true)

        "true || false".eval().assertEqualsTo(true)
        "false || true".eval().assertEqualsTo(true)
        "false || false".eval().assertEqualsTo(false)

        "true || true || false".eval().assertEqualsTo(true)
        "false || true || true".eval().assertEqualsTo(true)
        "false || false || true".eval().assertEqualsTo(true)
        "false || false || false".eval().assertEqualsTo(false)
    }

    @Test
    fun and_or_order() {
        "true && true || false".eval().assertEqualsTo(true)
        "false || true && true".eval().assertEqualsTo(true)

        "(false && true) || (true && true)".eval().assertEqualsTo(true)
        "false && true || true && true".eval().assertEqualsTo(true)
        "(false && true || true) && true".eval().assertEqualsTo(true)

        "true || false && false".eval().assertEqualsTo(true)
        "(true || false) && false".eval().assertEqualsTo(false)
    }

    @Test
    fun with_different_source() {
        "false || 1 == 1".eval().assertEqualsTo(true)
        "true && 1 == 2".eval().assertEqualsTo(false)

        "(1 == 2) || false || (1+1) == 2".eval().assertEqualsTo(true)
        "1 == 2 || false || (1+1) == 2".eval().assertEqualsTo(true)
        "1 == 1 && 2 == 2".eval().assertEqualsTo(true)

        "1 == 2 && 2 == 1 || 2 * 2 == 4".eval().assertEqualsTo(true)
    }
}