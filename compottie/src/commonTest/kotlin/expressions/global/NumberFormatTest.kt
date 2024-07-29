package expressions.global

import expressions.assertSimpleExprEquals
import kotlin.test.Test

class NumberFormatTest {

    @Test
    fun test(){
        "123".assertSimpleExprEquals(123L)
        "123.1".assertSimpleExprEquals(123.1)
        ".1".assertSimpleExprEquals(.1)

        "0xff".assertSimpleExprEquals(255L)
        "0b11".assertSimpleExprEquals(3L)
        "0o123".assertSimpleExprEquals(83L)
    }
}