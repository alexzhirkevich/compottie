package js

import kotlin.test.Test


class SubTest {

    @Test
    fun numbers() {
        "13-17".runJs().assertEqualsTo(-4L)
        "13 - 17".runJs().assertEqualsTo(-4L)
        "-13-17".runJs().assertEqualsTo(-30L)
        "13.0-17.0".runJs().assertEqualsTo(-4.0)
        "13 - 17.0".runJs().assertEqualsTo(-4.0)
        "-13.0 -17".runJs().assertEqualsTo(-30.0)
    }

    @Test
    fun string(){
        "'10' - '3'".runJs().assertEqualsTo(7L)
        "10 - '3'".runJs().assertEqualsTo(7L)
        "'10' - 3".runJs().assertEqualsTo(7L)

        "'10.5' - '3'".runJs().assertEqualsTo(7.5)
        "10.5 - '3'".runJs().assertEqualsTo(7.5)
        "'10.5' - 3".runJs().assertEqualsTo(7.5)
        "'qsd' - 3".runJs().assertEqualsTo(Double.NaN)
    }

    @Test
    fun null_undefined(){
        "null - 5".runJs().assertEqualsTo(-5L)
        "5 - null".runJs().assertEqualsTo(5L)
        "null - null".runJs().assertEqualsTo(0L)
        "null - '123'".runJs().assertEqualsTo(-123L)
        "null - '123.5'".runJs().assertEqualsTo(-123.5)

        "undefined - 5".runJs().assertEqualsTo(Double.NaN)
        "5 - undefined".runJs().assertEqualsTo(Double.NaN)
        "undefined - undefined".runJs().assertEqualsTo(Double.NaN)
    }
}

