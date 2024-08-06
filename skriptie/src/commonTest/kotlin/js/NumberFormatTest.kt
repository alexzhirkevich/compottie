package js

import kotlin.test.Test
import kotlin.test.assertTrue

class NumberFormatTest {

    @Test
    fun test(){
        "123".eval().assertEqualsTo(123L)
        "123.1".eval().assertEqualsTo(123.1)
        ".1".eval().assertEqualsTo(.1)

        "0xff".eval().assertEqualsTo(255L)
        "0b11".eval().assertEqualsTo(3L)
        "0o123".eval().assertEqualsTo(83L)
    }

    @Test
    fun eq(){
        assertTrue { "5 === 5.0".eval() as Boolean }
    }
}