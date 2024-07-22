package expressions.js

import expressions.assertExprEquals
import kotlin.test.Test

internal class JsStringExpressionsTest {

    @Test
    fun endsWith() {
        "'test123'.endsWith('123')".assertExprEquals(true)
        "'test123'.endsWith('1234')".assertExprEquals(false)
        "'test1234'.endsWith('123', 7)".assertExprEquals(true)
    }

    @Test
    fun startsWith() {
        "'123test'.startsWith('123')".assertExprEquals(true)
        "'123test'.startsWith('1234')".assertExprEquals(false)
        "'F1234test'.startsWith('123', 1)".assertExprEquals(true)
    }

    @Test
    fun includes() {
        "'x123test'.includes('123')".assertExprEquals(true)
        "'x123test'.includes('1234')".assertExprEquals(false)
    }

    @Test
    fun match() {
        "'a'.match('[a-z]')".assertExprEquals(true)
        "'A'.match('[a-z]')".assertExprEquals(false)
    }

    @Test
    fun padEnd() {
        "'abc'.padEnd(5))".assertExprEquals("abc  ")
        "'abc'.padEnd(5,'0'))".assertExprEquals("abc00")
        "'abc'.padEnd(5,'12'))".assertExprEquals("abc12")
        "'abc'.padEnd(6,'12'))".assertExprEquals("abc121")
        "'abcdef'.padEnd(5,'0'))".assertExprEquals("abcdef")
    }

    @Test
    fun padStart() {
        "'abc'.padStart(5))".assertExprEquals("  abc")
        "'abc'.padStart(5,'0'))".assertExprEquals("00abc")
        "'abc'.padStart(5,'12'))".assertExprEquals("12abc")
        "'abc'.padStart(6,'12'))".assertExprEquals("121abc")
        "'abcdef'.padStart(5,'0'))".assertExprEquals("abcdef")
    }

    @Test
    fun repeat() {
        "'abc'.repeat(3))".assertExprEquals("abcabcabc")
        "'abc'.repeat(0))".assertExprEquals("")
    }

    @Test
    fun replace() {
        "'aabbcc'.replace('b','f'))".assertExprEquals("aafbcc")
        "'aabbcc'.replace('x','ff'))".assertExprEquals("aabbcc")
        "'aabbcc'.replace('','ff'))".assertExprEquals("ffaabbcc")
    }

    @Test
    fun replaceAll() {
        "'aabbcc'.replaceAll('b','f'))".assertExprEquals("aaffcc")
        "'aabbcc'.replaceAll('x','ff'))".assertExprEquals("aabbcc")
        "'aabbcc'.replaceAll('','ff'))".assertExprEquals("ffaabbcc")
    }

    @Test
    fun trim() {
        "' abc '.trim())".assertExprEquals("abc")
        "'abc '.trim())".assertExprEquals("abc")
        "' abc'.trim())".assertExprEquals("abc")

        "' abc'.trimStart())".assertExprEquals("abc")
        "' abc '.trimStart())".assertExprEquals("abc ")
        "'abc '.trimStart())".assertExprEquals("abc ")

        "' abc'.trimEnd())".assertExprEquals(" abc")
        "' abc '.trimEnd())".assertExprEquals(" abc")
        "'abc '.trimEnd())".assertExprEquals("abc")
    }
    @Test
    fun substring() {
        "'123456'.substring(1)".assertExprEquals("23456")
        "'123456'.substring(2)".assertExprEquals("3456")
        "'123456'.substring(3)".assertExprEquals("456")
        "'123456'.substring(0)".assertExprEquals("123456")

        "'123456'.substring(1,2)".assertExprEquals("2")
        "'123456'.substring(1,3)".assertExprEquals("23")
        "'123456'.substring(1,4)".assertExprEquals("234")
        "'123456'.substring(0,10)".assertExprEquals("123456")
        "'123456'.substring(0,3)".assertExprEquals("123")
    }
}