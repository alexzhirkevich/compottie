package js

import kotlin.test.Test

class NumberFormatTest {

    @Test
    fun test(){
        "123".runJs().assertEqualsTo(123L)
        "123.1".runJs().assertEqualsTo(123.1)
        ".1".runJs().assertEqualsTo(.1)

        "0xff".runJs().assertEqualsTo(255L)
        "0b11".runJs().assertEqualsTo(3L)
        "0o123".runJs().assertEqualsTo(83L)
    }
}