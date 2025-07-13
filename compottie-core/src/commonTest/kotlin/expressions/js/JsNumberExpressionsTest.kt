package expressions.js

import expressions.assertExprEquals
import kotlin.test.Test

internal class JsNumberExpressionsTest {

    @Test
    fun toFixed(){
        "12.12345.toFixed()".assertExprEquals("12")
        "12.52345.toFixed()".assertExprEquals("13")
        "12.12345.toFixed(1)".assertExprEquals("12.1")
        "12.12345.toFixed(3)".assertExprEquals("12.123")
        "123.456.toFixed(2)".assertExprEquals("123.46")
        "123.51.toFixed(1)".assertExprEquals("123.5")
    }

    @Test
    fun toPrecision(){
        val num = 5.123456;
        "$num.toPrecision()".assertExprEquals(5.123456f)
        "$num.toPrecision(5)".assertExprEquals(5.1235f)
        "$num.toPrecision(2)".assertExprEquals(5.1f)
        "$num.toPrecision(1)".assertExprEquals(5f)
    }
}