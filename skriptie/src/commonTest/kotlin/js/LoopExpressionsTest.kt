package js

import kotlin.test.Test

class LoopExpressionsTest {

    @Test
    fun whileLoop() {
        """
            var x = 0
            while(x != 3) {
                x += 1
            }
            x
        """.trimIndent().runJs().assertEqualsTo(3L)

        """
            var x = 0
            while(x < 3)
                x += 1
            x
        """.trimIndent().runJs().assertEqualsTo(3L)
    }

    @Test
    fun doWhileLoop() {
        """
            var x = 0
            do {
                x+=1
            } while(x != 3)
            x
        """.trimIndent().runJs().assertEqualsTo(3L)
    }
}