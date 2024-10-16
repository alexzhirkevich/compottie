package js

import kotlin.test.Test

class BitwiseTest {

    @Test
    fun bitwise(){
        "8 >> 2".eval().assertEqualsTo(2L)
        "4 << 2".eval().assertEqualsTo(16L)
        "36 >>> 4".eval().assertEqualsTo(2L)
        "5 & 3".eval().assertEqualsTo(1L)
        "5 | 3".eval().assertEqualsTo(7L)
        "5 ^ 3".eval().assertEqualsTo(6L)
        "~5".eval().assertEqualsTo(-6L)
        "~-3".eval().assertEqualsTo(2L)
    }

    @Test
    fun bitwiseAssign(){
        "let x = 8; x >>= 2; x".eval().assertEqualsTo(2L);
        "let x = 4; x <<= 2;".eval().assertEqualsTo(16L)
        "let x = 36; x >>>= 2; x".eval().assertEqualsTo(9L)
        "let x = 5; x &= 3; x".eval().assertEqualsTo(1L)
        "let x = 5; x |= 3; x".eval().assertEqualsTo(7L)
        "let x = 5; x ^= 3; x".eval().assertEqualsTo(6L)
    }
}