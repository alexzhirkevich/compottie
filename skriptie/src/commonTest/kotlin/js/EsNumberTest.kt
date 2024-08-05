package js

import kotlin.test.Test

class EsNumberTest {

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

    @Test
    fun type(){
        "typeof(Number)".eval().assertEqualsTo("function")
        "Number(123)".eval().assertEqualsTo(123L)
        "Number(123.0)".eval().assertEqualsTo(123.0)
        "Number(\"123\")".eval().assertEqualsTo(123L)
        "Number(\"123.0\")".eval().assertEqualsTo(123.0)
        "Number(\"unicorn\")".eval().assertEqualsTo(Double.NaN)
        "Number(undefined)".eval().assertEqualsTo(Double.NaN)
    }
    @Test
    fun static_props(){

        "Number.MAX_SAFE_INTEGER".eval().assertEqualsTo(Long.MAX_VALUE)
        "Number.MIN_SAFE_INTEGER".eval().assertEqualsTo(Long.MIN_VALUE)
        "Number.MAX_VALUE".eval().assertEqualsTo(Double.MAX_VALUE)
        "Number.EPSILON".eval().assertEqualsTo(Double.MIN_VALUE)
        "Number.POSITIVE_INFINITY".eval().assertEqualsTo(Double.POSITIVE_INFINITY)
        "Number.NEGATIVE_INFINITY".eval().assertEqualsTo(Double.NEGATIVE_INFINITY)
        "Number.NaN".eval().assertEqualsTo(Double.NaN)
    }

    @Test
    fun static_methods(){
        "Number.isFinite(123)".eval().assertEqualsTo(true)
        "Number.isInteger(123)".eval().assertEqualsTo(true)
        "Number.isInteger(123.3)".eval().assertEqualsTo(false)
        "Number.isNan(123.3)".eval().assertEqualsTo(false)
        "Number.isNan(NaN)".eval().assertEqualsTo(true)
        "Number.isSafeInteger(123.3)".eval().assertEqualsTo(false)
        "Number.isSafeInteger(123)".eval().assertEqualsTo(true)
        "Number.parseFloat('123.3')".eval().assertEqualsTo(123.3)
        "Number.parseFloat('123.3sdfsdf')".eval().assertEqualsTo(123.3)
        "Number.parseInt('123')".eval().assertEqualsTo(123L)
        "Number.parseInt('123.3')".eval().assertEqualsTo(123L)
        "Number.parseInt('123.3sdfsdf')".eval().assertEqualsTo(123L)
        "Number.parseInt(' 0xff', 16)".eval().assertEqualsTo(255L)
    }
}