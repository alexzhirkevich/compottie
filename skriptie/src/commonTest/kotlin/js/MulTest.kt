package js

import kotlin.test.Test


class MulTest {

    @Test
    fun numbers() {
        "13 * 17.0".runJs().assertEqualsTo(221.0)
        "-13 * -17".runJs().assertEqualsTo(221L)
        "-13.0 * 17 * 2".runJs().assertEqualsTo(-442.0)
    }

    @Test
    fun string(){
        "'10' * '3'".runJs().assertEqualsTo(30L)
        "10 * '3'".runJs().assertEqualsTo(30L)
        "'10' * 3".runJs().assertEqualsTo(30L)

        "'10.5' * '3'".runJs().assertEqualsTo(31.5)
        "10.5 * '3'".runJs().assertEqualsTo(31.5)
        "'10.5' * 3".runJs().assertEqualsTo(31.5)
        "'qsd' * 3".runJs().assertEqualsTo(Double.NaN)
    }

    @Test
    fun null_undefined(){
        "null * 5".runJs().assertEqualsTo(0L)
        "5 * null".runJs().assertEqualsTo(0L)
        "null * null".runJs().assertEqualsTo(0L)

        "undefined * null".runJs().assertEqualsTo(Double.NaN)
        "5 * undefined".runJs().assertEqualsTo(Double.NaN)
        "undefined * undefined".runJs().assertEqualsTo(Double.NaN)
    }
}

