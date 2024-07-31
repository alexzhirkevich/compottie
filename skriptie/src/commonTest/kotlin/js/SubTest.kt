package js

import kotlin.test.Test


class SubTest {

    @Test
    fun randomJsPizdec(){
        "10 - [[[[[[[[[[5]]]]]]]]]]".eval().assertEqualsTo(5L)
    }

    @Test
    fun numbers() {
        "13-17".eval().assertEqualsTo(-4L)
        "13 - 17".eval().assertEqualsTo(-4L)
        "-13-17".eval().assertEqualsTo(-30L)
        "13.0-17.0".eval().assertEqualsTo(-4.0)
        "13 - 17.0".eval().assertEqualsTo(-4.0)
        "-13.0 -17".eval().assertEqualsTo(-30.0)
    }

    @Test
    fun string(){
        "'10' - '3'".eval().assertEqualsTo(7L)
        "10 - '3'".eval().assertEqualsTo(7L)
        "'10' - 3".eval().assertEqualsTo(7L)

        "'10.5' - '3'".eval().assertEqualsTo(7.5)
        "10.5 - '3'".eval().assertEqualsTo(7.5)
        "'10.5' - 3".eval().assertEqualsTo(7.5)
        "'qsd' - 3".eval().assertEqualsTo(Double.NaN)
    }

    @Test
    fun null_undefined(){
        "null - 5".eval().assertEqualsTo(-5L)
        "5 - null".eval().assertEqualsTo(5L)
        "null - null".eval().assertEqualsTo(0L)
        "null - '123'".eval().assertEqualsTo(-123L)
        "null - '123.5'".eval().assertEqualsTo(-123.5)

        "undefined - 5".eval().assertEqualsTo(Double.NaN)
        "5 - undefined".eval().assertEqualsTo(Double.NaN)
        "undefined - undefined".eval().assertEqualsTo(Double.NaN)
    }
}

