package js

import kotlin.test.Test


class DivTest {

    @Test
    fun numbers() {
        "26 / 2.0".eval().assertEqualsTo(13.0)
        "26 / 2".eval().assertEqualsTo(13L)
        "-26 / 2.0".eval().assertEqualsTo(-13.0)
        "-26 / 2".eval().assertEqualsTo(-13L)

        "-26 / -2.0".eval().assertEqualsTo(13.0)
        "-26 / -2".eval().assertEqualsTo(13L)
        "-52 / -2.0 / 2".eval().assertEqualsTo(13.0)

        "10/0".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
        "10.0/0".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
        "10/0.0".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
    }

    @Test
    fun string() {
        "'30' / '3'".eval().assertEqualsTo(10L)
        "30.0 / '3'".eval().assertEqualsTo(10.0)
        "'30' / 3.0".eval().assertEqualsTo(10.0)

        "'30' / 0".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
        "'30' / 0.0".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
    }

    @Test
    fun null_undefined() {
        "null / 5".eval().assertEqualsTo(0L)
        "5 / null".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
        "0 / null".eval().assertEqualsTo(Double.NaN)
        "null / 0".eval().assertEqualsTo(Double.NaN)
        "null / null".eval().assertEqualsTo(Double.NaN)

        "undefined / null".eval().assertEqualsTo(Double.NaN)
        "5 / undefined".eval().assertEqualsTo(Double.NaN)
        "undefined / undefined".eval().assertEqualsTo(Double.NaN)

        "'qsd' / 3".eval().assertEqualsTo(Double.NaN)
        "'0' / null".eval().assertEqualsTo(Double.NaN)
        "null / '0'".eval().assertEqualsTo(Double.NaN)
    }
}