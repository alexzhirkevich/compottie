package js

import kotlin.test.Test

class JsNumberTest {

    @Test
    fun toFixed(){
        "12.12345.toFixed()".eval().assertEqualsTo("12")
        "12.52345.toFixed()".eval().assertEqualsTo("13")
        "12.12345.toFixed(1)".eval().assertEqualsTo("12.1")
        "12.12345.toFixed(3)".eval().assertEqualsTo("12.123")
        "123.456.toFixed(2)".eval().assertEqualsTo("123.46")
        "123.51.toFixed(1)".eval().assertEqualsTo("123.5")
    }

    @Test
    fun toPrecision(){
        val num = 5.123456;
        "$num.toPrecision()".eval().assertEqualsTo(5.123456)
        "$num.toPrecision(5)".eval().assertEqualsTo(5.1235)
        "$num.toPrecision(2)".eval().assertEqualsTo(5.1)
        "$num.toPrecision(1)".eval().assertEqualsTo(5.0)
    }
}