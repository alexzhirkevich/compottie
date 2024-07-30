package js

import expressions.assertExprReturns
import expressions.ret
import kotlin.test.Test

class LoopExpressionsTest {

    @Test
    fun whileLoop() {
        """
            var $ret = 0
            while($ret != 3) {
                $ret += 1
            }
        """.trimIndent().assertExprReturns(3L)

        """
            var $ret = 0
            while($ret < 3)
                $ret += 1

        """.trimIndent().assertExprReturns(3L)
    }

    @Test
    fun doWhileLoop() {
        """
            var $ret = 0
            do {
                $ret+=1
            } while($ret != 3)
        """.trimIndent().assertExprReturns(3L)
    }
}