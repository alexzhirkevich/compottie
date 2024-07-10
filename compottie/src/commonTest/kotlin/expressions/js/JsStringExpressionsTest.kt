package expressions.js

import expressions.assertExprValueEquals
import kotlin.test.Test

internal class JsStringExpressionsTest {

    @Test
    fun endsWith() {
        "'test123'.endsWith('123')".assertExprValueEquals(true)
        "'test123'.endsWith('1234')".assertExprValueEquals(false)
        "'test1234'.endsWith('123', 7)".assertExprValueEquals(true)
    }

    @Test
    fun startsWith() {
        "'123test'.startsWith('123')".assertExprValueEquals(true)
        "'123test'.startsWith('1234')".assertExprValueEquals(false)
        "'F1234test'.startsWith('123', 1)".assertExprValueEquals(true)
    }

    @Test
    fun includes() {
        "'x123test'.includes('123')".assertExprValueEquals(true)
        "'x123test'.includes('1234')".assertExprValueEquals(false)
    }

    @Test
    fun match() {
        "'a'.match('[a-z]')".assertExprValueEquals(true)
        "'A'.match('[a-z]')".assertExprValueEquals(false)
    }

    @Test
    fun padEnd() {
        "'abc'.padEnd(5))".assertExprValueEquals("abc  ")
        "'abc'.padEnd(5,'0'))".assertExprValueEquals("abc00")
        "'abc'.padEnd(5,'12'))".assertExprValueEquals("abc12")
        "'abc'.padEnd(6,'12'))".assertExprValueEquals("abc121")
        "'abcdef'.padEnd(5,'0'))".assertExprValueEquals("abcdef")
    }

    @Test
    fun padStart() {
        "'abc'.padStart(5))".assertExprValueEquals("  abc")
        "'abc'.padStart(5,'0'))".assertExprValueEquals("00abc")
        "'abc'.padStart(5,'12'))".assertExprValueEquals("12abc")
        "'abc'.padStart(6,'12'))".assertExprValueEquals("121abc")
        "'abcdef'.padStart(5,'0'))".assertExprValueEquals("abcdef")
    }

    @Test
    fun repeat() {
        "'abc'.repeat(3))".assertExprValueEquals("abcabcabc")
        "'abc'.repeat(0))".assertExprValueEquals("")
    }

    @Test
    fun replace() {
        "'aabbcc'.replace('b','f'))".assertExprValueEquals("aafbcc")
        "'aabbcc'.replace('x','ff'))".assertExprValueEquals("aabbcc")
        "'aabbcc'.replace('','ff'))".assertExprValueEquals("ffaabbcc")
    }

    @Test
    fun replaceAll() {
        "'aabbcc'.replaceAll('b','f'))".assertExprValueEquals("aaffcc")
        "'aabbcc'.replaceAll('x','ff'))".assertExprValueEquals("aabbcc")
        "'aabbcc'.replaceAll('','ff'))".assertExprValueEquals("ffaabbcc")
    }

    @Test
    fun trim() {
        "' abc '.trim())".assertExprValueEquals("abc")
        "'abc '.trim())".assertExprValueEquals("abc")
        "' abc'.trim())".assertExprValueEquals("abc")

        "' abc'.trimStart())".assertExprValueEquals("abc")
        "' abc '.trimStart())".assertExprValueEquals("abc ")
        "'abc '.trimStart())".assertExprValueEquals("abc ")

        "' abc'.trimEnd())".assertExprValueEquals(" abc")
        "' abc '.trimEnd())".assertExprValueEquals(" abc")
        "'abc '.trimEnd())".assertExprValueEquals("abc")
    }
    @Test
    fun substring() {
        "'123456'.substring(1)".assertExprValueEquals("23456")
        "'123456'.substring(2)".assertExprValueEquals("3456")
        "'123456'.substring(3)".assertExprValueEquals("456")
        "'123456'.substring(0)".assertExprValueEquals("123456")

        "'123456'.substring(1,2)".assertExprValueEquals("2")
        "'123456'.substring(1,3)".assertExprValueEquals("23")
        "'123456'.substring(1,4)".assertExprValueEquals("234")
        "'123456'.substring(0,10)".assertExprValueEquals("123456")
        "'123456'.substring(0,3)".assertExprValueEquals("123")
    }
}