package expressions.global

import expressions.assertExprEquals
import kotlin.test.Test

internal class NumberFormatTest {

    @Test
    fun test(){
        "123".assertExprEquals(123f)
        "123.1".assertExprEquals(123.1f)
        ".1".assertExprEquals(.1f)

        "0xff".assertExprEquals(255f)
        "0b11".assertExprEquals(3f)
        "0o123".assertExprEquals(83f)
    }
}