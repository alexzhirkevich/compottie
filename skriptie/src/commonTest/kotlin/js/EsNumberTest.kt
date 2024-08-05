package js

import kotlin.test.Test
import kotlin.test.assertTrue

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
    fun static_methods() {

        listOf("Number.", "globalThis.", "").forEach {
            "${it}isFinite(123)".eval().assertEqualsTo(true)
            "${it}isInteger(123)".eval().assertEqualsTo(true)
            "${it}isInteger(123.3)".eval().assertEqualsTo(false)
            "${it}isNan(123.3)".eval().assertEqualsTo(false)
            "${it}isNan(NaN)".eval().assertEqualsTo(true)
            "${it}isSafeInteger(123.3)".eval().assertEqualsTo(false)
            "${it}isSafeInteger(123)".eval().assertEqualsTo(true)
            "${it}parseFloat('123.3')".eval().assertEqualsTo(123.3)
            "${it}parseFloat('123.3sdfsdf')".eval().assertEqualsTo(123.3)
            "${it}parseInt('123')".eval().assertEqualsTo(123L)
            "${it}parseInt('123.3')".eval().assertEqualsTo(123L)
            "${it}parseInt('123.3sdfsdf')".eval().assertEqualsTo(123L)
            "${it}parseInt(' 0xff', 16)".eval().assertEqualsTo(255L)
            "${it}parseInt(' 0xff', 0)".eval().assertEqualsTo(255L)
            "${it}parseInt(' 0xffhh', 16)".eval().assertEqualsTo(255L)
            "${it}parseInt(' 110', 2)".eval().assertEqualsTo(6L)
            "${it}parseInt('1245', 8)".eval().assertEqualsTo(677L)
        }
    }

    @Test
    fun methods_in_global_Scope(){
        assertTrue { "Number.isFinite == isFinite".eval() as Boolean }
        assertTrue { "Number.isFinite == globalThis.isFinite".eval() as Boolean }
        assertTrue { "Number.isNan == isNan".eval() as Boolean }
        assertTrue { "Number.isNan == globalThis.isNan".eval() as Boolean }
        assertTrue { "Number.parseFloat == parseFloat".eval() as Boolean }
        assertTrue { "Number.parseFloat == globalThis.parseFloat".eval() as Boolean }
        assertTrue { "Number.isInteger == isInteger".eval() as Boolean }
        assertTrue { "Number.isInteger == globalThis.isInteger".eval() as Boolean }
        assertTrue { "Number.isSafeInteger == isSafeInteger".eval() as Boolean }
        assertTrue { "Number.isSafeInteger == globalThis.isSafeInteger".eval() as Boolean }
        assertTrue { "Number.parseInt == parseInt".eval() as Boolean }
        assertTrue { "Number.parseInt == globalThis.parseInt".eval() as Boolean }
    }
}