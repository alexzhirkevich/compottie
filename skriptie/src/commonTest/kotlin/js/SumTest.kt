package js

import kotlin.test.Test


class SumTest {

    @Test
    fun randomJsPizdec(){
        "'b' + 'a' + + 'a' + 'a'".eval().assertEqualsTo("baNaNa")
        "[5] + 1".eval().assertEqualsTo("51")
        "[1,2] + [3,3]".eval().assertEqualsTo("1,23,3")
    }

    @Test
    fun numbers() {
        "13+17".eval().assertEqualsTo(30L)
        "-13+ 17".eval().assertEqualsTo(4L)
        "-13+ -17".eval().assertEqualsTo(-30L)
        "-13+ -17.0 + 10 - 4.0".eval().assertEqualsTo(-24.0)
    }

    @Test
    fun string(){
        "'10' + '5'".eval().assertEqualsTo("105")
        "'10' + 5".eval().assertEqualsTo("105")
        "10 + '5'".eval().assertEqualsTo("105")
    }

    @Test
    fun null_undefined(){
        "null + 5".eval().assertEqualsTo(5L)
        "5 + null".eval().assertEqualsTo(5L)
        "null + null".eval().assertEqualsTo(0L)

        "undefined + 5".eval().assertEqualsTo(Double.NaN)
        "5 + undefined".eval().assertEqualsTo(Double.NaN)
        "undefined + undefined".eval().assertEqualsTo(Double.NaN)
    }
}

