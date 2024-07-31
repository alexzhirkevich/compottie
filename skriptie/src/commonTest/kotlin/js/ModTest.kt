package js

import kotlin.test.Test


class ModTest {

    @Test
    fun numbers() {
        "13 % 7".eval().assertEqualsTo(6L)
        "13 % -7".eval().assertEqualsTo(6L)
        "-13 % 7".eval().assertEqualsTo(-6L)
        "-13 % -7".eval().assertEqualsTo(-6L)

        "13 % 0".eval().assertEqualsTo(Double.NaN)
        "0 % 0".eval().assertEqualsTo(Double.NaN)
        "0 % 13".eval().assertEqualsTo(0L)
    }

    @Test
    fun string() {
        "'13' % '7'".eval().assertEqualsTo(6L)
        "'13' % 7".eval().assertEqualsTo(6L)
        "13 % '7'".eval().assertEqualsTo(6L)
    }

    @Test
    fun null_undefined() {
        "null % 5".eval().assertEqualsTo(0L)
        "5  % null".eval().assertEqualsTo(Double.NaN)
        "0 % null".eval().assertEqualsTo(Double.NaN)
        "null % 0".eval().assertEqualsTo(Double.NaN)
        "null % null".eval().assertEqualsTo(Double.NaN)

        "undefined % null".eval().assertEqualsTo(Double.NaN)
        "5 % undefined".eval().assertEqualsTo(Double.NaN)
        "undefined % undefined".eval().assertEqualsTo(Double.NaN)
    }
}