package js

import kotlin.test.Test


class MulTest {

    @Test
    fun numbers() {
        "13 * 17.0".eval().assertEqualsTo(221.0)
        "-13 * -17".eval().assertEqualsTo(221L)
        "-13.0 * 17 * 2".eval().assertEqualsTo(-442.0)
    }

    @Test
    fun string(){
        "'10' * '3'".eval().assertEqualsTo(30L)
        "10 * '3'".eval().assertEqualsTo(30L)
        "'10' * 3".eval().assertEqualsTo(30L)

        "'10.5' * '3'".eval().assertEqualsTo(31.5)
        "10.5 * '3'".eval().assertEqualsTo(31.5)
        "'10.5' * 3".eval().assertEqualsTo(31.5)
        "'qsd' * 3".eval().assertEqualsTo(Double.NaN)
    }

    @Test
    fun null_undefined(){
        "null * 5".eval().assertEqualsTo(0L)
        "5 * null".eval().assertEqualsTo(0L)
        "null * null".eval().assertEqualsTo(0L)

        "undefined * null".eval().assertEqualsTo(Double.NaN)
        "5 * undefined".eval().assertEqualsTo(Double.NaN)
        "undefined * undefined".eval().assertEqualsTo(Double.NaN)
    }
}

