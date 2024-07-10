package expressions.js

import expressions.assertExprValueEquals
import kotlin.test.Test

internal class JsCommonExpressionsTest {

    @Test
    fun indexOf() {
        "'abc'.indexOf('b')".assertExprValueEquals(1)
        "'abc'.indexOf('d')".assertExprValueEquals(-1)

        "[1,2,3].indexOf(2)".assertExprValueEquals(1)
        "[1,2,3].indexOf(4)".assertExprValueEquals(-1)

        "'abbbc'.indexOf('b')".assertExprValueEquals(1)
        "'abbbc'.lastIndexOf('b')".assertExprValueEquals(3)
        "'abbbc'.lastIndexOf('f')".assertExprValueEquals(-1)
    }

    @Test
    fun toStringTest() {
        "'abc'.toString()".assertExprValueEquals("abc")
        "123.toString()".assertExprValueEquals("123")
        "(-123).toString()".assertExprValueEquals("-123")
        "(-123.0).toString()".assertExprValueEquals("-123.0")
        "[1,2,3].toString()".assertExprValueEquals("[1, 2, 3]")
        "[].toString()".assertExprValueEquals("[]")
    }
}