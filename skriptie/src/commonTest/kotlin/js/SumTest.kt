package js

import kotlin.test.Test
import kotlin.test.assertEquals


class SumTest {

    @Test
    fun numbers() {
        "13+17".runJs().assertEqualsTo(30L)
        "-13+ 17".runJs().assertEqualsTo(4L)
        "-13+ -17".runJs().assertEqualsTo(-30L)
        "-13+ -17.0 + 10 - 4.0".runJs().assertEqualsTo(-24.0)
    }

    @Test
    fun string(){
        "'10' + '5'".runJs().assertEqualsTo("105")
        "'10' + 5".runJs().assertEqualsTo("105")
        "10 + '5'".runJs().assertEqualsTo("105")
    }

    @Test
    fun null_undefined(){
        "null + 5".runJs().assertEqualsTo(5L)
        "5 + null".runJs().assertEqualsTo(5L)
        "null + null".runJs().assertEqualsTo(0L)

        "undefined + 5".runJs().assertEqualsTo(Double.NaN)
        "5 + undefined".runJs().assertEqualsTo(Double.NaN)
        "undefined + undefined".runJs().assertEqualsTo(Double.NaN)
    }
}

