package expressions.js

import expressions.assertExprValueEquals
import kotlin.test.Test

internal class JsNumberExpressionsTest {

    @Test
    fun toFixed(){
        "12.12345.toFixed()".assertExprValueEquals("12")
        "12.52345.toFixed()".assertExprValueEquals("13")
        "12.12345.toFixed(1)".assertExprValueEquals("12.1")
        "12.12345.toFixed(3)".assertExprValueEquals("12.123")
        "123.456.toFixed(2)".assertExprValueEquals("123.46")
        "123.51.toFixed(1)".assertExprValueEquals("123.5")
    }

    @Test
    fun toPrecision(){
        val num = 5.123456;
        "$num.toPrecision()".assertExprValueEquals(5.123456f)
        "$num.toPrecision(5)".assertExprValueEquals(5.1235f)
        "$num.toPrecision(2)".assertExprValueEquals(5.1f)
        "$num.toPrecision(1)".assertExprValueEquals(5f)
    }
}