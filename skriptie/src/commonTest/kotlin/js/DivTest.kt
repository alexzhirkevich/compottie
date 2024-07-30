package js

import kotlin.test.Test


class DivTest {

    @Test
    fun numbers() {
        "26 / 2.0".runJs().assertEqualsTo(13.0)
        "26 / 2".runJs().assertEqualsTo(13L)
        "-26 / 2.0".runJs().assertEqualsTo(-13.0)
        "-26 / 2".runJs().assertEqualsTo(-13L)

        "-26 / -2.0".runJs().assertEqualsTo(13.0)
        "-26 / -2".runJs().assertEqualsTo(13L)
        "-52 / -2.0 / 2".runJs().assertEqualsTo(13.0)

        "10/0".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)
        "10.0/0".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)
        "10/0.0".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)
    }

    @Test
    fun string(){
        "'30' / '3'".runJs().assertEqualsTo(10L)
        "30.0 / '3'".runJs().assertEqualsTo(10.0)
        "'30' / 3.0".runJs().assertEqualsTo(10.0)

        "'30' / 0".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)
        "'30' / 0.0".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)

        "'qsd' / 3".runJs().assertEqualsTo(Double.NaN)
        "'0' / null".runJs().assertEqualsTo(Double.NaN)
        "null / '0'".runJs().assertEqualsTo(Double.NaN)
    }

    @Test
    fun null_undefined(){
        "null / 5".runJs().assertEqualsTo(0L)
        "5 / null".runJs().assertEqualsTo(Double.POSITIVE_INFINITY)
        "0 / null".runJs().assertEqualsTo(Double.NaN)
        "null / 0".runJs().assertEqualsTo(Double.NaN)
        "null / null".runJs().assertEqualsTo(Double.NaN)

        "undefined / null".runJs().assertEqualsTo(Double.NaN)
        "5 / undefined".runJs().assertEqualsTo(Double.NaN)
        "undefined / undefined".runJs().assertEqualsTo(Double.NaN)
    }
}

