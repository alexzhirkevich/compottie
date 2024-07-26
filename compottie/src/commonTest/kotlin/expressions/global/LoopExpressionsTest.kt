package expressions.global

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
        """.trimIndent().assertExprReturns(3)

//        """
//            var $ret = 0
//            while($ret < 3)
//                $ret += 1
//
//        """.trimIndent().assertExprReturns(3)
    }
}