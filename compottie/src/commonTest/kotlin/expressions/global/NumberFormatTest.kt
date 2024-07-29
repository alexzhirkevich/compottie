package expressions.global

import expressions.assertExprEquals
import expressions.assertSimpleExprEquals
import expressions.assertSimpleExprReturns
import kotlin.test.Test

class NumberFormatTest {

    @Test
    fun test(){
        "123".assertSimpleExprEquals(123)
        "123.1".assertSimpleExprEquals(123.1f)
        ".1".assertSimpleExprEquals(.1f)

        "0xff".assertSimpleExprEquals(255)
        "0b11".assertSimpleExprEquals(3)
        "0o123".assertSimpleExprEquals(83)
    }
}