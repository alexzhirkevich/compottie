package js

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsArrayTest {

    @Test
    fun forEach(){
        """
            let x = 0
            let arr = [1,2,3]
            arr.forEach(v => x+=v)
            x
        """.trimIndent().eval().assertEqualsTo(6L)

        """
            let x = 0
            let arr = [1,2,3]
            arr.forEach(() => x++)
            x
        """.trimIndent().eval().assertEqualsTo(3L)
    }

    @Test
    fun map(){
        """
            let arr = [1,2,3]
            arr.map(v => v * v)
        """.trimIndent().eval().assertEqualsTo(listOf(1L,4L,9L))

        """
            let arr = [1,2,3]
            arr.map(() => 1)
        """.trimIndent().eval().assertEqualsTo(listOf(1L,1L,1L))
    }

    @Test
    fun filter(){
        """
            let arr = [1,2,3,4]
            arr.filter(v => v % 2 === 0)
        """.trimIndent().eval().assertEqualsTo(listOf(2L, 4L))

        """
            let arr = [1,2,3,4]
            arr.filter(() => 1)
        """.trimIndent().eval().assertEqualsTo(listOf(1L,2L,3L,4L))
    }

    @Test
    fun some(){
        assertTrue {
            """
                let arr = [1,2,3,4]
                arr.some(v => v === 2)
            """.trimIndent().eval() as Boolean
        }

        assertFalse {
            """
                let arr = [1,2,3,4]
                arr.some(v => v === 5)
            """.trimIndent().eval() as Boolean
        }
    }

    @Test
    fun sort(){
        """
            let arr = [66,2,8]
            arr.sort()
            arr
        """.trimIndent().eval().assertEqualsTo(listOf(2L,8L, 66L))
    }

    @Test
    fun at(){
        "[66,2,8].at(0)".eval().assertEqualsTo(66L)
        "[66,2,8].at(3)".eval().assertEqualsTo(Unit)

        "[66,2,8][1]".eval().assertEqualsTo(2L)
        "[66,2,8][3]".eval().assertEqualsTo(Unit)
    }
}