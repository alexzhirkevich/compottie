package expressions.js

import expressions.assertExprEquals
import kotlin.test.Test

internal class JsCommonExpressionsTest {

    @Test
    fun indexOf() {
        "'abc'.indexOf('b')".assertExprEquals(1)
        "'abc'.indexOf('d')".assertExprEquals(-1)

        "[1,2,3].indexOf(2)".assertExprEquals(1)
        "[1,2,3].indexOf(4)".assertExprEquals(-1)

        "'abbbc'.indexOf('b')".assertExprEquals(1)
        "'abbbc'.lastIndexOf('b')".assertExprEquals(3)
        "'abbbc'.lastIndexOf('f')".assertExprEquals(-1)
    }

    @Test
    fun toStringTest() {
        "'abc'.toString()".assertExprEquals("abc")
        "123.toString()".assertExprEquals("123")
        "(-123).toString()".assertExprEquals("-123")
        "(-123.0).toString()".assertExprEquals("-123.0")
        "[1,2,3].toString()".assertExprEquals("[1, 2, 3]")
        "[].toString()".assertExprEquals("[]")
    }
}